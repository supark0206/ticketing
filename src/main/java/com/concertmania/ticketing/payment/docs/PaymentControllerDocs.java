package com.concertmania.ticketing.payment.docs;

import com.concertmania.ticketing.payment.dto.ConfirmPaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentResponse;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentControllerDocs {

    @Operation(
            summary = "결제 시작",
            description = "좌석 예약 후 결제를 시작합니다. PG사로 결제 요청을 보내고 결제 대기 상태로 변경됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 시작 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class),
                            examples = @ExampleObject(
                                    name = "결제 시작 성공 예시",
                                    value = """
                                    {
                                        "reservationId": 1,
                                        "status": "PAYMENT_PENDING",
                                        "amount": 50000,
                                        "pgRequestSuccess": true,
                                        "transactionId": "txn_123456789"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "유효성 검사 실패",
                                    value = """
                                    {
                                        "status": 400,
                                        "message": "결제 금액은 필수입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                    {
                                        "status": 401,
                                        "message": "로그인이 필요합니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "좌석을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "좌석 없음",
                                    value = """
                                    {
                                        "status": 404,
                                        "message": "좌석을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "좌석 상태 충돌 - 이미 예약된 좌석",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "좌석 충돌",
                                    value = """
                                    {
                                        "status": 409,
                                        "message": "이미 예약된 좌석입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "좌석 점유 시간 만료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "좌석 점유 만료",
                                    value = """
                                    {
                                        "status": 422,
                                        "message": "좌석 점유 시간이 만료되었습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<PaymentResponse> startPayment(
            @Parameter(
                    description = "결제 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "카드 결제 요청",
                                            value = """
                                            {
                                                "seatId": 1,
                                                "paymentMethod": "CARD",
                                                "amount": 50000
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "계좌이체 결제 요청",
                                            value = """
                                            {
                                                "seatId": 2,
                                                "paymentMethod": "BANK_TRANSFER",
                                                "amount": 30000
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    );

    @Operation(
            summary = "결제 완료 처리 (Webhook)",
            description = "PG사로부터 결제 완료 알림을 받아 결제를 완료 처리합니다. 내부 시스템용 웹훅 API입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 완료 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "결제 완료 처리 성공",
                                    value = """
                                    {
                                        "message": "결제 완료 처리 성공"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "유효성 검사 실패",
                                    value = """
                                    {
                                        "status": 400,
                                        "message": "거래 ID는 필수입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "결제 정보 없음",
                                    value = """
                                    {
                                        "status": 404,
                                        "message": "결제 정보를 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 처리된 결제",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "결제 중복 처리",
                                    value = """
                                    {
                                        "status": 409,
                                        "message": "이미 처리된 결제입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "예약 만료 또는 이미 처리된 예약",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "예약 만료",
                                    value = """
                                    {
                                        "status": 422,
                                        "message": "예약이 만료되었습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<PaymentResponse> confirmPayment(
            @Parameter(
                    description = "결제 완료 확인 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConfirmPaymentRequest.class),
                            examples = @ExampleObject(
                                    name = "결제 완료 확인 요청",
                                    value = """
                                    {
                                        "transactionId": "txn_123456789"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody ConfirmPaymentRequest request
    );
}