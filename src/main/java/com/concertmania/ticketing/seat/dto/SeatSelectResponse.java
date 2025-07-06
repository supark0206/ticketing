package com.concertmania.ticketing.seat.dto;

import com.concertmania.ticketing.seat.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "좌석 선택 응답 DTO")
public class SeatSelectResponse {

    @Schema(description = "좌석 ID", example = "1")
    private Long id;

    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;

    @Schema(description = "콘서트 제목", example = "IU 콘서트")
    private String concertTitle;

    @Schema(description = "좌석 구역", example = "A")
    private String section;

    @Schema(description = "좌석 행", example = "1")
    private String row;

    @Schema(description = "좌석 번호", example = "15")
    private String number;

    @Schema(description = "좌석 등급", example = "VIP")
    private String grade;

    @Schema(description = "좌석 가격", example = "150000")
    private BigDecimal price;

    @Schema(description = "예약 여부", example = "false")
    private boolean isReserved;

    @Schema(description = "생성 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "좌석 선택 만료 시간", example = "2024-01-01T10:05:00")
    private LocalDateTime expiresAt; // 좌석 만료 시간

    public static SeatSelectResponse from(Seat seat, LocalDateTime expiresAt, boolean isReserved) {
        return new SeatSelectResponse(
                seat.getId(),
                seat.getConcert().getId(),
                seat.getConcert().getTitle(),
                seat.getSection(),
                seat.getRow(),
                seat.getNumber(),
                seat.getGrade(),
                seat.getPrice(),
                isReserved,
                seat.getCreatedAt(),
                seat.getUpdatedAt(),
                expiresAt
        );
    }
}