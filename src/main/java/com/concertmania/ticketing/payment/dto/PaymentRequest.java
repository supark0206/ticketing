package com.concertmania.ticketing.payment.dto;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long seatId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
}