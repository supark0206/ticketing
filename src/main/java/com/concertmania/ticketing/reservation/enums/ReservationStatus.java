package com.concertmania.ticketing.reservation.enums;

public enum ReservationStatus {
    IN_PROGRESS("진행중"),  // 예약 요청이 들어왔지만 아직 처리되지 않은 상태
    CONFIRMED("확정"),      // 예약이 확정된 상태
    FAILED("실패"),         // 예약이 실패된 상태
    CANCELED("취소/환불"),   // 사용자가 예약을 취소한 상태
    EXPIRED("만료")         // 시간 만료로 예약이 무효화된 상태
    ;

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}