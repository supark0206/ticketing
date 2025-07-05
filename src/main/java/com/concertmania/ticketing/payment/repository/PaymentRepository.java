package com.concertmania.ticketing.payment.repository;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
