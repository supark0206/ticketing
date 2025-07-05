package com.concertmania.ticketing.seat.repository;

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

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QSeat seat = QSeat.seat;

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

}