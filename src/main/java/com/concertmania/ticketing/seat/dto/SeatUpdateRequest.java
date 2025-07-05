package com.concertmania.ticketing.seat.dto;

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
public class SeatUpdateRequest {

    @NotBlank(message = "구역은 필수입니다.")
    private String section;

    @NotBlank(message = "행은 필수입니다.")
    private String row;

    @NotBlank(message = "번호는 필수입니다.")
    private String number;

    @NotBlank(message = "등급은 필수입니다.")
    private String grade;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    private BigDecimal price;
}