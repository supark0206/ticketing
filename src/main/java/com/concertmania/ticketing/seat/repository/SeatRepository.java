package com.concertmania.ticketing.seat.repository;

import com.concertmania.ticketing.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {

    Optional<Seat> findByIdAndDeletedAtIsNull(Long id);

}