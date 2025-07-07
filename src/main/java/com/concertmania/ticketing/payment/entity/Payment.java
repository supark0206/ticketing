package com.concertmania.ticketing.payment.entity;

import com.concertmania.ticketing.payment.enums.PaymentMethod;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.utils.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_reservation_id", columnList = "reservation_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;      // 예약 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;         // 결제 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;         // 결제 수단

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;            // 결제 금액

    @Column(unique = true)
    private String transactionId;         // 결제 ID , 이후 PG사 연동 확장성 고려

    @Column(nullable = false)
    private LocalDateTime paymentTime;    // 결제 시간  

    @PrePersist
    protected void onCreate() {
        this.paymentTime = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}