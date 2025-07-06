package com.concertmania.ticketing.payment.dto;

import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 요청 정보")
public class PaymentRequest {
    @NotNull(message = "좌석 ID는 필수입니다.")
    @Schema(description = "좌석 ID", example = "1")
    private Long seatId;
    
    @NotNull(message = "결제 방법은 필수입니다.")
    @Schema(description = "결제 방법", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    @Schema(description = "결제 금액", example = "50000")
    private BigDecimal amount;
}