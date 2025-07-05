package com.concertmania.ticketing.concert.dto;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertResponse {

    private Long id;
    private String title;
    private LocalDateTime concertDate;
    private String venue;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private ConcertStatus status;
    private LocalDateTime createdAt;
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