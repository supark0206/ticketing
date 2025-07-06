package com.concertmania.ticketing.payment.repository;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

}
