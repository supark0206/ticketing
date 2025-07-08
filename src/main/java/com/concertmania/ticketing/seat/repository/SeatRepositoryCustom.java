package com.concertmania.ticketing.seat.repository;

import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.entity.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SeatRepositoryCustom {

    // 콘서트별 좌석 조회 - 페이징 - 미삭제
    Page<Seat> findByConcertIdNotDeletedPage(Long concertId, Pageable pageable);

    // 콘서트별 좌석 조회 - 미삭제
    List<Seat> findByConcertIdNotDeleted(Long concertId);

    // 콘서트별 좌석 조회 - 검색 - 미삭제
    List<Seat> findByConcertIdAndGradeNotDeleted(Long concertId, String grade);

    // 콘서트별 좌석 중복 검증
    boolean existsByConcertIdAndSeats(Long concertId, List<SeatCreateRequest> requests);

    // 예약 불가능 좌석 검증
    boolean hasActiveReservations(Long id);

    // 좌석 목록의 예약 상태를 한 번에 조회 (성능 개선)
    Map<Long, Boolean> getActiveReservationStatusBySeatIds(List<Long> seatIds);

    // 예약 가능 상태의 좌석 카운팅
    List<Seat> findByAvailableSeat(Long concertId);

}