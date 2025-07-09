package com.concertmania.ticketing.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 확인 응답 정보")
public class ConfirmPaymentResponse {
    @Schema(description = "결제 거래 ID", example = "TXN_12345678")
    private String transactionId;

    @Schema(description = "결제 성공 여부", example = "true")
    private boolean success;
}