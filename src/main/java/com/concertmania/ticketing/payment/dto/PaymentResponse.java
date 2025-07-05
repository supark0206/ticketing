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
    
    @Schema(description = "예약 ID", example = "1")
    private Long reservationId;
    
    @Schema(description = "예약 상태", example = "CONFIRMED")
    private ReservationStatus status;
    
    @Schema(description = "결제 금액", example = "50000")
    private BigDecimal amount;
    
    @Schema(description = "결제 성공 여부", example = "true")
    private boolean success;
}