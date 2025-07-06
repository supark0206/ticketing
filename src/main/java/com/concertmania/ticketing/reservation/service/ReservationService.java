package com.concertmania.ticketing.reservation.service;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.payment.repository.PaymentRepository;
import com.concertmania.ticketing.reservation.dto.CancelReservationRequest;
import com.concertmania.ticketing.reservation.dto.CancelReservationResponse;
import com.concertmania.ticketing.reservation.dto.ReservationResponse;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.reservation.repository.ReservationRepository;
import com.concertmania.ticketing.reservation.repository.ReservationSeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 사용자별 예약 조회 (본인 예약만)
     */
    public Page<ReservationResponse> getUserReservations(User user, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByUserIdWithDetails(user.getId(), pageable);
        
        return reservations.map(reservation -> {
            List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservation(reservation);
            BigDecimal totalAmount = calculateTotalAmount(reservationSeats);
            return ReservationResponse.from(reservation, reservationSeats, totalAmount);
        });
    }

    /**
     * 어드민용 모든 예약 조회
     */
    public Page<ReservationResponse> getAllReservations(User user, Pageable pageable) {
        // 어드민 권한 확인
        if (user.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Page<Reservation> reservations = reservationRepository.findAllWithDetails(pageable);
        
        return reservations.map(reservation -> {
            List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservation(reservation);
            BigDecimal totalAmount = calculateTotalAmount(reservationSeats);
            return ReservationResponse.from(reservation, reservationSeats, totalAmount);
        });
    }

    /**
     * 예약 상세 조회
     */
    public ReservationResponse getReservationDetail(Long reservationId, User user) {
        Reservation reservation;

        // 어드민은 모든 예약 조회 가능, 일반 사용자는 본인 예약만 조회 가능
        if (user.getRole() == UserRole.ADMIN) {
            reservation = reservationRepository.findByIdWithDetails(reservationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        } else {
            reservation = reservationRepository.findByIdAndUserIdWithDetails(reservationId, user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        }

        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservation(reservation);
        BigDecimal totalAmount = calculateTotalAmount(reservationSeats);

        return ReservationResponse.from(reservation, reservationSeats, totalAmount);
    }

    /**
     * 예약 취소 (사용자 본인만 가능)
     */
    @Transactional
    public CancelReservationResponse cancelReservation(CancelReservationRequest request, User user) {
        // 사용자 본인의 예약인지 확인
        Reservation reservation = reservationRepository.findByIdAndUserIdWithDetails(request.getReservationId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 취소 가능한 상태인지 확인
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 공연 시작 전인지 확인 (공연 2시간 전까지만 취소 가능)
        LocalDateTime concertDate = reservation.getConcert().getConcertDate();
        LocalDateTime cancelDeadline = concertDate.minusHours(2);
        
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 결제 정보 조회 및 상태 변경
        Payment payment = paymentRepository.findByReservation(reservation)
                .orElse(null);

        BigDecimal refundAmount = BigDecimal.ZERO;
        
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            // 결제 상태를 환불로 변경
            payment.updateStatus(PaymentStatus.REFUNDED);
            refundAmount = payment.getAmount();
            log.info("[결제 환불 처리] Payment ID: {}, Amount: {}", payment.getId(), refundAmount);
        }

        // 예약 상태를 취소로 변경
        reservation.updateStatus(ReservationStatus.CANCELED);
        
        log.info("[예약 취소 완료] Reservation ID: {}, User ID: {}, Refund Amount: {}", 
                reservation.getId(), user.getId(), refundAmount);

        return CancelReservationResponse.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .refundAmount(refundAmount)
                .success(true)
                .message("예약이 성공적으로 취소되었습니다.")
                .build();
    }

    /**
     * 콘서트별 예약 조회 (어드민용)
     */
    public Page<ReservationResponse> getReservationsByConcert(Long concertId, User user, Pageable pageable) {
        // 어드민 권한 확인
        if (user.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Page<Reservation> reservations = reservationRepository.findByConcertIdWithDetails(concertId, pageable);
        
        return reservations.map(reservation -> {
            List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservation(reservation);
            BigDecimal totalAmount = calculateTotalAmount(reservationSeats);
            return ReservationResponse.from(reservation, reservationSeats, totalAmount);
        });
    }

    /**
     * 예약 좌석의 총 금액 계산
     */
    private BigDecimal calculateTotalAmount(List<ReservationSeat> reservationSeats) {
        return reservationSeats.stream()
                .map(rs -> rs.getSeat().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}