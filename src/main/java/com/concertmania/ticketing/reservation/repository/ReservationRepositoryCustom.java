package com.concertmania.ticketing.reservation.repository;

import com.concertmania.ticketing.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryCustom {

    // 사용자별 예약 조회 (본인 예약만)
    Page<Reservation> findByUserIdWithDetails(Long userId, Pageable pageable);

    // 어드민용 모든 예약 조회
    Page<Reservation> findAllWithDetails(Pageable pageable);

    // 예약 상세 조회 (단건)
    Optional<Reservation> findByIdWithDetails(Long reservationId);

    // 사용자별 예약 상세 조회 (권한 확인용)
    Optional<Reservation> findByIdAndUserIdWithDetails(Long reservationId, Long userId);

    // 콘서트별 예약 조회
    Page<Reservation> findByConcertIdWithDetails(Long concertId, Pageable pageable);
}