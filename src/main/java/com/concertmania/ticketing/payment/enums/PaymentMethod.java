package com.concertmania.ticketing.payment.enums;

public enum PaymentMethod {
    CREDIT_CARD("신용카드"),    // 신용카드
    BANK_TRANSFER("계좌이체")  // 계좌이체
    ;

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

