package com.concertmania.ticketing.seat.dto;

import com.concertmania.ticketing.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectResponse {

    private Long id;
    private Long concertId;
    private String concertTitle;
    private String section;
    private String row;
    private String number;
    private String grade;
    private BigDecimal price;
    private boolean isReserved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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