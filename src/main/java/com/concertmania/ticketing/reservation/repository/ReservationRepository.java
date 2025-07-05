package com.concertmania.ticketing.reservation.repository;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findById(Long id);
}
