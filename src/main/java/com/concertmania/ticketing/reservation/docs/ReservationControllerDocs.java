package com.concertmania.ticketing.reservation.docs;

import com.concertmania.ticketing.reservation.dto.CancelReservationRequest;
import com.concertmania.ticketing.reservation.dto.CancelReservationResponse;
import com.concertmania.ticketing.reservation.dto.ReservationResponse;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "Reservation", description = "예약 관련 API")
public interface ReservationControllerDocs {

    @Operation(
            summary = "내 예약 조회",
            description = "로그인한 사용자의 예약 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Page<ReservationResponse>> getMyReservations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "페이징 정보")
            Pageable pageable
    );

    @Operation(
            summary = "모든 예약 조회 (관리자용)",
            description = "관리자가 모든 사용자의 예약을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "모든 예약 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자 권한 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "페이징 정보")
            Pageable pageable
    );

    @Operation(
            summary = "예약 상세 조회",
            description = "특정 예약의 상세 정보를 조회합니다. 일반 사용자는 본인 예약만, 관리자는 모든 예약을 조회할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예약을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ReservationResponse> getReservationDetail(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long reservationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    );

    @Operation(
            summary = "예약 취소",
            description = "사용자가 본인의 예약을 취소합니다. 결제된 금액은 환불 처리됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CancelReservationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "취소 불가능한 예약",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예약을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<CancelReservationResponse> cancelReservation(
            @Parameter(
                    description = "예약 취소 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CancelReservationRequest.class)
                    )
            )
            @Valid @RequestBody CancelReservationRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    );

    @Operation(
            summary = "콘서트별 예약 조회 (관리자용)",
            description = "관리자가 특정 콘서트의 모든 예약을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "콘서트별 예약 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자 권한 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Page<ReservationResponse>> getReservationsByConcert(
            @Parameter(description = "콘서트 ID", example = "1")
            @PathVariable Long concertId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "페이징 정보")
            Pageable pageable
    );
}