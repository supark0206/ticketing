package com.concertmania.ticketing.reservation.dto;

import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "예약 응답")
public class ReservationResponse {

    @Schema(description = "예약 ID", example = "1")
    private Long reservationId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String userEmail;

    @Schema(description = "콘서트 제목", example = "BTS 콘서트")
    private String concertTitle;

    @Schema(description = "콘서트 일시", example = "2024-12-25T19:00:00")
    private LocalDateTime concertDate;

    @Schema(description = "예약 상태", example = "CONFIRMED")
    private ReservationStatus status;

    @Schema(description = "예약 시간", example = "2024-01-15T14:30:00")
    private LocalDateTime reservationTime;

    @Schema(description = "만료 시간", example = "2024-01-15T14:40:00")
    private LocalDateTime expireTime;

    @Schema(description = "좌석 목록")
    private List<SeatInfo> seats;

    @Schema(description = "총 결제 금액", example = "50000")
    private BigDecimal totalAmount;

    @Getter
    @Builder
    @Schema(description = "좌석 정보")
    public static class SeatInfo {
        @Schema(description = "좌석 ID", example = "1")
        private Long seatId;

        @Schema(description = "구역", example = "A")
        private String section;

        @Schema(description = "열", example = "1")
        private String row;

        @Schema(description = "번호", example = "5")
        private String number;

        @Schema(description = "등급", example = "VIP")
        private String grade;

        @Schema(description = "가격", example = "50000")
        private BigDecimal price;
    }

    public static ReservationResponse from(Reservation reservation, List<ReservationSeat> reservationSeats, BigDecimal totalAmount) {
        List<SeatInfo> seats = reservationSeats.stream()
                .map(rs -> SeatInfo.builder()
                        .seatId(rs.getSeat().getId())
                        .section(rs.getSeat().getSection())
                        .row(rs.getSeat().getRow())
                        .number(rs.getSeat().getNumber())
                        .grade(rs.getSeat().getGrade())
                        .price(rs.getSeat().getPrice())
                        .build())
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .userEmail(reservation.getUser().getEmail())
                .concertTitle(reservation.getConcert().getTitle())
                .concertDate(reservation.getConcert().getConcertDate())
                .status(reservation.getStatus())
                .reservationTime(reservation.getReservationTime())
                .expireTime(reservation.getExpireTime())
                .seats(seats)
                .totalAmount(totalAmount)
                .build();
    }
}