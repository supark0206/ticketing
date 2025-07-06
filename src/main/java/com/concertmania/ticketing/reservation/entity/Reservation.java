package com.concertmania.ticketing.reservation.entity;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservation")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservationTime;

    @Column(nullable = false)
    private LocalDateTime expireTime;

    @PrePersist
    protected void onCreate() {
        this.reservationTime = LocalDateTime.now();
        this.expireTime = this.reservationTime.plusMinutes(10);
    }
    
    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}