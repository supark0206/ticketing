package com.concertmania.ticketing.concert.repository;

import com.concertmania.ticketing.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertRepositoryCustom {

    Optional<Concert> findByIdAndDeletedAtIsNull(Long id);
}