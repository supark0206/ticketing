package com.concertmania.ticketing.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예약 취소 요청")
public class CancelReservationRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    @Schema(description = "취소할 예약 ID", example = "1")
    private Long reservationId;

    @Schema(description = "취소 사유", example = "개인 사정으로 인한 취소")
    private String cancelReason;
}