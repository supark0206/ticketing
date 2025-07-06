package com.concertmania.ticketing.reservation.repository;

import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    List<ReservationSeat> findByReservation(Reservation reservation);
}
