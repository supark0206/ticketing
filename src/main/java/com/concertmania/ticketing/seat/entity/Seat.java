package com.concertmania.ticketing.seat.entity;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.utils.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat", indexes = {
    @Index(name = "idx_seat_concert_id", columnList = "concert_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private String row;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Seat update(String section, String row, String number, String grade, BigDecimal price) {
        this.section = section;
        this.row = row;
        this.number = number;
        this.grade = grade;
        this.price = price;
        return this;
    }
}