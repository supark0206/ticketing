package com.concertmania.ticketing.reservation.dto;

import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "예약 취소 응답")
public class CancelReservationResponse {

    @Schema(description = "취소된 예약 ID", example = "1")
    private Long reservationId;

    @Schema(description = "예약 상태", example = "CANCELED")
    private ReservationStatus status;

    @Schema(description = "환불 금액", example = "50000")
    private BigDecimal refundAmount;

    @Schema(description = "취소 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메시지", example = "예약이 성공적으로 취소되었습니다.")
    private String message;
}