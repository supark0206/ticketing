package com.concertmania.ticketing.concert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘서트 수정 요청 DTO")
public class ConcertUpdateRequest {

    @NotBlank(message = "콘서트 제목은 필수입니다.")
    @Schema(description = "콘서트 제목", example = "아이유 콘서트 2024 (수정됨)")
    private String title;

    @NotNull(message = "콘서트 날짜는 필수입니다.")
    @Schema(description = "콘서트 날짜", example = "2024-12-26T19:00:00")
    private LocalDateTime concertDate;

    @NotBlank(message = "공연 장소는 필수입니다.")
    @Schema(description = "공연 장소", example = "잠실 실내체육관")
    private String venue;

    @NotNull(message = "예매 오픈 시간은 필수입니다.")
    @Schema(description = "예매 오픈 시간", example = "2024-12-01T10:00:00")
    private LocalDateTime openTime;

    @NotNull(message = "예매 마감 시간은 필수입니다.")
    @Schema(description = "예매 마감 시간", example = "2024-12-25T23:59:59")
    private LocalDateTime closeTime;
}