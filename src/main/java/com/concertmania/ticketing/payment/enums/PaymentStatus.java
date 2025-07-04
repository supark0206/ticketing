package com.concertmania.ticketing.payment.enums;

public enum PaymentStatus {
    IN_PROGRESS("결제진행중"),  // 결제가 진행 중인 상태
    SUCCESS("결제완료"),       // 결제가 성공적으로 완료된 상태
    FAILED("결제실패"),        // 결제가 실패한 상태
    CANCELED("결제취소");      // 결제가 취소된 상태

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
