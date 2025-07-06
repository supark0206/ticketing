package com.concertmania.ticketing.payment.service;

import com.concertmania.ticketing.notification.NotificationProducer;
import com.concertmania.ticketing.payment.dto.ConfirmPaymentResponse;
import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentResponse;
import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.payment.repository.PaymentRepository;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.reservation.repository.ReservationRepository;
import com.concertmania.ticketing.reservation.repository.ReservationSeatRepository;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationProducer notificationProducer;

    @Transactional
    public PaymentResponse startPayment(PaymentRequest request, User user) {

        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(request.getSeatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // 예약 좌석 검증
        if (seatRepository.hasActiveReservations(request.getSeatId())) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_RESERVED_SEAT);
        }

        // Redis 락 확인 및 소유권 검증
        String redisKey = "seat_lock:" + seat.getId();
        String lockedUserId = redisTemplate.opsForValue().get(redisKey);
        if (lockedUserId == null || !lockedUserId.equals(String.valueOf(user.getId()))) {
            throw new CustomException(ErrorCode.SEAT_NOT_SELECTED);
        }

        // Reservation 생성 - 결제 진행 상태
        Reservation reservation = Reservation.builder()
                .user(user)
                .concert(seat.getConcert())
                .status(ReservationStatus.IN_PROGRESS)
                .build();
        reservationRepository.save(reservation);

        // ReservationSeat 생성
        ReservationSeat reservationSeat = ReservationSeat.builder()
                .reservation(reservation)
                .seat(seat)
                .build();
        reservationSeatRepository.save(reservationSeat);

        // Payment 생성 (결제 진행중 상태)
        String transactionId = "TXN_" + System.currentTimeMillis() + "_" + user.getId();
        Payment payment = Payment.builder()
                .reservation(reservation)
                .status(PaymentStatus.IN_PROGRESS)
                .method(request.getPaymentMethod())
                .amount(request.getAmount())
                .transactionId(transactionId)
                .build();
        paymentRepository.save(payment);

        log.info("[결제 시작] User: {}, Seat: {}, TransactionId: {}",
                user.getId(), seat.getId(), transactionId);

        return PaymentResponse.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .amount(request.getAmount())
                .transactionId(transactionId)
                .pgRequestSuccess(true) // PG 호출 성공 여부 (실제 연동 시 다르게 처리)
                .build();
    }

    @Transactional
    public ConfirmPaymentResponse confirmPayment(String transactionId, boolean paymentSuccess) {

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        Reservation reservation = payment.getReservation();

        // 상태 검증 - 이미 처리된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("[결제 상태 오류] 이미 처리된 결제. TransactionId: {}, Status: {}",
                    transactionId, payment.getStatus());
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            log.warn("[예약 상태 오류] 이미 처리된 예약. ReservationId: {}, Status: {}",
                    reservation.getId(), reservation.getStatus());
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_PROCESSED);
        }

        // 예약 만료 시간 검증
        if (reservation.getExpireTime().isBefore(LocalDateTime.now())) {
            payment.updateStatus(PaymentStatus.FAILED);
            reservation.updateStatus(ReservationStatus.EXPIRED);
            cleanupRedisLock(reservation, "예약 시간 만료");
            log.warn("[예약 만료] TransactionId: {}, ExpireTime: {}",
                    transactionId, reservation.getExpireTime());
            throw new CustomException(ErrorCode.RESERVATION_EXPIRED);
        }

        // 예약 좌석 정보 조회
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservation(reservation);
        if (reservationSeats.isEmpty()) {
            throw new CustomException(ErrorCode.RESERVATION_SEAT_NOT_FOUND);
        }

        // 현재는 단일 좌석 예약 시스템
        Seat seat = reservationSeats.get(0).getSeat();
        User user = reservation.getUser();

        // 결제 결과에 따른 상태 업데이트
        if (paymentSuccess) {
            payment.updateStatus(PaymentStatus.SUCCESS); // 결제 성공
            reservation.updateStatus(ReservationStatus.CONFIRMED); // 예약성공

            log.info("[결제 성공] User: {}, Seat: {}, TransactionId: {}",
                    user.getId(), seat.getId(), transactionId);

            // 이메일 발송 처리
            notificationProducer.sendEmailNotification(
                    user.getEmail(),
                    "[ConcertMania] 예매 완료 안내",
                    String.format("%s님, 콘서트 예매가 완료되었습니다. 좌석: %s구역 %s열 %s번",
                            user.getEmail(),
                            seat.getSection(),
                            seat.getRow(),
                            seat.getNumber()
                    )
            );

        } else {
            payment.updateStatus(PaymentStatus.FAILED); // 결제 실패
            reservation.updateStatus(ReservationStatus.FAILED); // 예약 실패

            log.warn("[결제 실패] User: {}, Seat: {}, TransactionId: {}",
                    user.getId(), seat.getId(), transactionId);
        }

        // Redis 락 해제 (성공/실패 관계없이)
        cleanupRedisLock(reservation, paymentSuccess ? "결제 성공" : "결제 실패");

        return ConfirmPaymentResponse.builder()
                .transactionId(transactionId)
                .isSuccess(paymentSuccess)
                .build();
    }

    // 레디스 락 정리 메소드
    private void cleanupRedisLock(Reservation reservation, String reason) {
        List<ReservationSeat> seats = reservationSeatRepository.findByReservation(reservation);
        User user = reservation.getUser();

        for (ReservationSeat reservationSeat : seats) {
            String redisKey = "seat_lock:" + reservationSeat.getSeat().getId();
            String lockUserId = redisTemplate.opsForValue().get(redisKey);

            // 락 소유권 확인 후 해제
            if (lockUserId != null && lockUserId.equals(String.valueOf(user.getId()))) {
                redisTemplate.delete(redisKey);
                log.info("[Redis 좌석 락 해제] Reason: {}, User: {}, Seat: {}",
                        reason, user.getId(), reservationSeat.getSeat().getId());
            } else if (lockUserId == null) {
                log.debug("[Redis 락 없음] 이미 만료되었거나 해제 됨. User: {}, Seat: {}",
                        user.getId(), reservationSeat.getSeat().getId());
            } else {
                log.warn("[Redis 락 소유권 불일치] 다른 사용자의 락. User: {}, Seat: {}, LockOwner: {}",
                        user.getId(), reservationSeat.getSeat().getId(), lockUserId);
            }

        }
    }
}
