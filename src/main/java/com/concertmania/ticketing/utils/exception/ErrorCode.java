package com.concertmania.ticketing.utils.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //400 BAD_REQUEST : 잘못된 요청
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"잘못된 요청입니다."),
    USER_REGISTER_ERROR(HttpStatus.BAD_REQUEST, "회원가입에 실패하였습니다."),

    //404 NOT_FOUND : 리소스를 찾을 수 없음
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    NOT_FOUND_USER_ID(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    //409 CONFLICT : 리소스 충돌
    EXIST_USER_EMAIL(HttpStatus.CONFLICT, "중복된 이메일이 존재합니다."),
    SEAT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 좌석입니다."),
    IS_ALREADY_RESERVATION(HttpStatus.CONFLICT, "이미 예약 된 좌석입니다."),
    SEAT_ALREADY_LOCKED(HttpStatus.CONFLICT, "다른 사용자가 이미 선택한 좌석입니다."),
    SEAT_LOCK_RETRY_NEEDED(HttpStatus.CONFLICT,"좌석 선점 중 충돌이 발생했습니다. 다시 시도해주세요."),
    SEAT_LOCK_EXPIRED(HttpStatus.CONFLICT, "좌석 점유 시간이 만료되었습니다. 다시 선택해주세요."),
    QUEUE_TOKEN_NOT_FOUND(HttpStatus.CONFLICT, "대기열 토큰을 찾을 수 없습니다."),
    QUEUE_NOT_ACTIVE(HttpStatus.CONFLICT, "활성 상태가 아닌 대기열입니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "이미 대기열에 등록되어 있습니다."),
    QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "대기열 정보를 찾을 수 없습니다."),
    SEAT_NOT_SELECTED(HttpStatus.NOT_FOUND, "선택된 좌석을 찾을 수 없습니다."),


    //405 METHOD_NOT_ALLOWED : 허용 되지않은 Request Method 호출
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드 호출입니다."),

    //422 UNPROCESSABLE_ENTITY : 비즈니스 로직 오류
    CONCERT_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "콘서트를 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "좌석을 찾을 수 없습니다."),
    INVALID_CONCERT_STATUS(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 콘서트 상태입니다."),
    RESERVATION_NOT_OPEN_YET(HttpStatus.UNPROCESSABLE_ENTITY, "아직 예매 오픈 시간이 아닙니다."),
    CANNOT_DELETE_RESERVATION_OPEN_CONCERT(HttpStatus.UNPROCESSABLE_ENTITY, "예매 오픈 중인 콘서트는 삭제할 수 없습니다."),
    CANNOT_DELETE_RESERVED_SEAT(HttpStatus.UNPROCESSABLE_ENTITY, "예약된 좌석은 삭제할 수 없습니다."),
    CONCERT_DATE_AFTER_CLOSE_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "공연일은 예약 마감일보다 이후여야 합니다."),
    CLOSE_TIME_AFTER_OPEN_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "예약 마감일은 예약 시작일보다 이후여야 합니다."),
    INVALID_REQUEST(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 요청입니다."),
    INVALID_CONCERT_ID(HttpStatus.UNPROCESSABLE_ENTITY, "모든 좌석은 동일한 콘서트에 속해야 합니다."),
    DUPLICATE_SEAT_IN_REQUEST(HttpStatus.UNPROCESSABLE_ENTITY, "요청 내에 중복된 좌석이 있습니다."),

    //500 INTERNAL_SERVER_ERROR : 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"내부 서버 오류입니다.")

    ;

    private final HttpStatus status;
    private final String MESSAGE;
}