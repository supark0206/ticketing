package com.concertmania.ticketing.payment.dto;

import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "결제 응답")
public class PaymentResponse {
    
    private Long reservationId;
    
    private ReservationStatus status;
    
    private BigDecimal amount;
    
    private boolean pgRequestSuccess;

    private String transactionId;
}