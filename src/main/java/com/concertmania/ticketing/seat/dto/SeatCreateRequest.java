package com.concertmania.ticketing.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "좌석 생성 요청 DTO")
public class SeatCreateRequest {

    @Schema(description = "콘서트 ID", example = "1", required = true)
    @NotNull(message = "콘서트 ID는 필수입니다.")
    private Long concertId;

    @Schema(description = "좌석 구역", example = "A", required = true)
    @NotBlank(message = "구역은 필수입니다.")
    private String section;

    @Schema(description = "좌석 행", example = "1", required = true)
    @NotBlank(message = "행은 필수입니다.")
    private String row;

    @Schema(description = "좌석 번호", example = "15", required = true)
    @NotBlank(message = "번호는 필수입니다.")
    private String number;

    @Schema(description = "좌석 등급", example = "VIP", required = true)
    @NotBlank(message = "등급은 필수입니다.")
    private String grade;

    @Schema(description = "좌석 가격", example = "150000", required = true)
    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    private BigDecimal price;
}