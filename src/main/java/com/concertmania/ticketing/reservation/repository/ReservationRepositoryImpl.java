package com.concertmania.ticketing.reservation.repository;

import com.concertmania.ticketing.concert.entity.QConcert;
import com.concertmania.ticketing.payment.entity.QPayment;
import com.concertmania.ticketing.reservation.entity.QReservation;
import com.concertmania.ticketing.reservation.entity.QReservationSeat;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.seat.entity.QSeat;
import com.concertmania.ticketing.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QReservation reservation = QReservation.reservation;
    private static final QReservationSeat reservationSeat = QReservationSeat.reservationSeat;
    private static final QUser user = QUser.user;
    private static final QConcert concert = QConcert.concert;
    private static final QSeat seat = QSeat.seat;
    private static final QPayment payment = QPayment.payment;

    @Override
    public Page<Reservation> findByUserIdWithDetails(Long userId, Pageable pageable) {
        List<Reservation> reservations = queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.concert, concert).fetchJoin()
                .where(reservation.user.id.eq(userId))
                .orderBy(reservation.reservationTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(reservation.user.id.eq(userId));

        return PageableExecutionUtils.getPage(reservations, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Reservation> findAllWithDetails(Pageable pageable) {
        List<Reservation> reservations = queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.concert, concert).fetchJoin()
                .orderBy(reservation.reservationTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(reservation.count())
                .from(reservation);

        return PageableExecutionUtils.getPage(reservations, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Reservation> findByIdWithDetails(Long reservationId) {
        Reservation result = queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.concert, concert).fetchJoin()
                .where(reservation.id.eq(reservationId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Reservation> findByIdAndUserIdWithDetails(Long reservationId, Long userId) {
        Reservation result = queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.concert, concert).fetchJoin()
                .where(
                        reservation.id.eq(reservationId),
                        reservation.user.id.eq(userId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Reservation> findByConcertIdWithDetails(Long concertId, Pageable pageable) {
        List<Reservation> reservations = queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.concert, concert).fetchJoin()
                .where(reservation.concert.id.eq(concertId))
                .orderBy(reservation.reservationTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(reservation.concert.id.eq(concertId));

        return PageableExecutionUtils.getPage(reservations, pageable, countQuery::fetchOne);
    }
}