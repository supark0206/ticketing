package com.concertmania.ticketing.concert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertUpdateRequest {

    @NotBlank(message = "콘서트 제목은 필수입니다.")
    private String title;

    @NotNull(message = "콘서트 날짜는 필수입니다.")
    private LocalDateTime concertDate;

    @NotBlank(message = "공연 장소는 필수입니다.")
    private String venue;

    @NotNull(message = "예매 오픈 시간은 필수입니다.")
    private LocalDateTime openTime;

    @NotNull(message = "예매 마감 시간은 필수입니다.")
    private LocalDateTime closeTime;
}