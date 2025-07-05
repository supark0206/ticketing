package com.concertmania.ticketing.concert.repository;

import com.concertmania.ticketing.concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertRepositoryCustom {

    Page<Concert> findAllNotDeleted(Pageable pageable);

    Page<Concert> findAllNotDeletedWithSearch(String title, String venue, Pageable pageable);

}