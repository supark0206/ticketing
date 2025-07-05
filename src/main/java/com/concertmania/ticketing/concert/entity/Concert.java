package com.concertmania.ticketing.concert.entity;

import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.utils.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "concert")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class Concert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime concertDate;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime openTime;

    @Column(nullable = false)
    private LocalDateTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertStatus status;

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    public Concert update(String title, LocalDateTime concertDate, String venue, LocalDateTime openTime, LocalDateTime closeTime) {
        this.title = title;
        this.concertDate = concertDate;
        this.venue = venue;
        this.openTime = openTime;
        this.closeTime = closeTime;
        return this;
    }

    public Concert markAsSoldOut() {
        this.status = ConcertStatus.SOLD_OUT;
        return this;
    }

    public Concert complete() {
        this.status = ConcertStatus.COMPLETED;
        return this;
    }

    public Concert cancel() {
        this.status = ConcertStatus.CANCELLED;
        return this;
    }
}