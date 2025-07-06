package com.concertmania.ticketing.concert.dto;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘서트 응답 DTO")
public class ConcertResponse {

    @Schema(description = "콘서트 ID", example = "1")
    private Long id;

    @Schema(description = "콘서트 제목", example = "아이유 콘서트 2024")
    private String title;

    @Schema(description = "콘서트 날짜", example = "2024-12-25T19:00:00")
    private LocalDateTime concertDate;

    @Schema(description = "공연 장소", example = "올림픽공원 체조경기장")
    private String venue;

    @Schema(description = "예매 오픈 시간", example = "2024-12-01T10:00:00")
    private LocalDateTime openTime;

    @Schema(description = "예매 마감 시간", example = "2024-12-24T23:59:59")
    private LocalDateTime closeTime;

    @Schema(description = "콘서트 상태", example = "ACTIVE")
    private ConcertStatus status;

    @Schema(description = "생성 일시", example = "2024-11-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-11-01T10:00:00")
    private LocalDateTime updatedAt;

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getConcertDate(),
                concert.getVenue(),
                concert.getOpenTime(),
                concert.getCloseTime(),
                concert.getStatus(),
                concert.getCreatedAt(),
                concert.getUpdatedAt()
        );
    }
}