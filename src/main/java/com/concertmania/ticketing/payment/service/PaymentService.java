package com.concertmania.ticketing.payment.service;

import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentResponse;
import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.payment.repository.PaymentRepository;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.reservation.repository.ReservationRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EntityManager entityManager;

    @Transactional
    public PaymentResponse paymentProcess(PaymentRequest request, User user) {

        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(request.getSeatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // Redis 락 확인
        String redisKey = "seat_lock:" + seat.getId();
        String lockedUserId = redisTemplate.opsForValue().get(redisKey);

        // 좌석 선점 검증
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
        
        entityManager.persist(reservationSeat);

        // PG 결제 모킹 → 임의로 성공 or 실패 랜덤하게 만듦
        boolean paymentSuccess = mockPaymentProcess();

        if (paymentSuccess) {
            reservation.updateStatus(ReservationStatus.CONFIRMED);
            
            // Payment 엔티티 생성
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .status(PaymentStatus.SUCCESS)
                    .method(request.getPaymentMethod())
                    .amount(request.getAmount())
                    .transactionId("TXN_" + System.currentTimeMillis())
                    .build();
            paymentRepository.save(payment);
            
            // Redis 락 해제 (결제 성공시에도 해제)
            redisTemplate.delete(redisKey);
            
            log.info("[결제 성공] User: {}, Seat: {}", user.getId(), seat.getId());
        } else {
            reservation.updateStatus(ReservationStatus.FAILED);
            
            // Payment 엔티티 생성 (실패 상태)
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .status(PaymentStatus.FAILED)
                    .method(request.getPaymentMethod())
                    .amount(request.getAmount())
                    .transactionId("TXN_" + System.currentTimeMillis())
                    .build();
            paymentRepository.save(payment);
            
            // Redis 락 해제
            redisTemplate.delete(redisKey);
            log.warn("[결제 실패] User: {}, Seat: {}", user.getId(), seat.getId());
        }

        return PaymentResponse.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .amount(request.getAmount())
                .success(paymentSuccess)
                .build();
    }

    private boolean mockPaymentProcess() {
        // 80% 성공률
        return Math.random() < 0.8;
    }
}
