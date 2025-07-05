package com.concertmania.ticketing.concert.repository;

import com.concertmania.ticketing.concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertRepositoryCustom {

    // 콘서트 목록 - 검색 - 페이징
    Page<Concert> findAllNotDeletedWithSearch(String title, String venue, Pageable pageable);

}