package com.concertmania.ticketing.payment.dto;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentResponse {
    private String transactionId;
    private boolean isSuccess;
}