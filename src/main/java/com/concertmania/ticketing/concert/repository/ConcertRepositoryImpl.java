package com.concertmania.ticketing.concert.repository;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.entity.QConcert;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QConcert concert = QConcert.concert;

    private BooleanExpression notDeleted() {
        return concert.deletedAt.isNull();
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? concert.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression venueContains(String venue) {
        return StringUtils.hasText(venue) ? concert.venue.containsIgnoreCase(venue) : null;
    }

    @Override
    public Page<Concert> findAllNotDeletedWithSearch(String title, String venue, Pageable pageable) {
        List<Concert> concerts = queryFactory
                .selectFrom(concert)
                .where(
                        notDeleted(),
                        titleContains(title),
                        venueContains(venue)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(concert.count())
                .from(concert)
                .where(
                        notDeleted(),
                        titleContains(title),
                        venueContains(venue)
                );

        return PageableExecutionUtils.getPage(concerts, pageable, countQuery::fetchOne);
    }

}