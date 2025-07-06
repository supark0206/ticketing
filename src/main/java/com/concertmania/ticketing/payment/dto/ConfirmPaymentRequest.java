package com.concertmania.ticketing.payment.dto;

import com.concertmania.ticketing.payment.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 확인 요청 정보")
public class ConfirmPaymentRequest {
    @Schema(description = "결제 거래 ID", example = "TXN_12345678")
    private String transactionId;
}