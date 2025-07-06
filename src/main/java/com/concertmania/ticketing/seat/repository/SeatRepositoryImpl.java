package com.concertmania.ticketing.seat.repository;

import com.concertmania.ticketing.reservation.entity.QReservation;
import com.concertmania.ticketing.reservation.entity.QReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.entity.QSeat;
import com.concertmania.ticketing.seat.entity.Seat;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QSeat seat = QSeat.seat;
    QReservationSeat reservationSeat = QReservationSeat.reservationSeat;
    QReservation reservation = QReservation.reservation;

    private BooleanExpression notDeleted() {
        return seat.deletedAt.isNull();
    }

    private BooleanExpression concertIdEq(Long concertId) {
        return seat.concert.id.eq(concertId);
    }

    private BooleanExpression gradeEq(String grade) {
        return seat.grade.eq(grade);
    }

    private BooleanExpression sectionEq(String section) {
        return seat.section.eq(section);
    }

    private BooleanExpression priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return seat.price.between(minPrice, maxPrice);
    }

    private BooleanExpression seatLocationEq(String section, String row, String number) {
        return seat.section.eq(section)
                .and(seat.row.eq(row))
                .and(seat.number.eq(number));
    }

    @Override
    public Page<Seat> findByConcertIdNotDeletedPage(Long concertId, Pageable pageable) {
        List<Seat> seats = queryFactory
                .selectFrom(seat)
                .where(
                        concertIdEq(concertId),
                        notDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(seat.count())
                .from(seat)
                .where(
                        concertIdEq(concertId),
                        notDeleted()
                );

        return PageableExecutionUtils.getPage(seats, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Seat> findByConcertIdNotDeleted(Long concertId) {
        return queryFactory
                .selectFrom(seat)
                .where(
                        concertIdEq(concertId),
                        notDeleted()
                )
                .orderBy(
                        seat.section.asc(),
                        seat.row.asc(),
                        seat.number.asc()
                )
                .fetch();
    }

    @Override
    public List<Seat> findByConcertIdAndGradeNotDeleted(Long concertId, String grade) {
        return queryFactory
                .selectFrom(seat)
                .where(
                        concertIdEq(concertId),
                        gradeEq(grade),
                        notDeleted()
                )
                .fetch();
    }

    @Override
    public boolean existsByConcertIdAndSeats(Long concertId, List<SeatCreateRequest> requests) {
        BooleanExpression seatConditions = null;

        for (SeatCreateRequest request : requests) {
            BooleanExpression condition = seat.section.eq(request.getSection())
                    .and(seat.row.eq(request.getRow()))
                    .and(seat.number.eq(request.getNumber()));

            seatConditions = seatConditions == null ? condition : seatConditions.or(condition);
        }

        // 해당 콘서트에서 요청된 좌석 중 하나라도 존재하는지 확인
        Integer count = queryFactory
                .selectOne()
                .from(seat)
                .where(seat.concert.id.eq(concertId)
                        .and(seatConditions))
                .fetchFirst();

        return count != null;
    }

    @Override
    public boolean hasActiveReservations(Long seatId) {
        QReservationSeat reservationSeat = QReservationSeat.reservationSeat;

        return queryFactory
                .selectOne()
                .from(reservationSeat)
                .where(
                        reservationSeat.seat.id.eq(seatId)
                                .and(reservationSeat.reservation.status.in(
                                        ReservationStatus.IN_PROGRESS,
                                        ReservationStatus.CONFIRMED
                                ))
                )
                .fetchFirst() != null;
    }

    @Override
    public Map<Long, Boolean> getActiveReservationStatusBySeatIds(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return new HashMap<>();
        }

        // 활성 예약이 있는 좌석 ID들 조회
        List<Long> reservedSeatIds = queryFactory
                .select(reservationSeat.seat.id)
                .from(reservationSeat)
                .where(
                        reservationSeat.seat.id.in(seatIds)
                                .and(reservationSeat.reservation.status.in(
                                        ReservationStatus.IN_PROGRESS,
                                        ReservationStatus.CONFIRMED
                                ))
                )
                .fetch();

        // 모든 좌석 ID에 대해 예약 상태 매핑
        return seatIds.stream()
                .collect(Collectors.toMap(
                        seatId -> seatId,
                        seatId -> reservedSeatIds.contains(seatId)
                ));
    }

}