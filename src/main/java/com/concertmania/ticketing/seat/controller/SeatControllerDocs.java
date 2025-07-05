package com.concertmania.ticketing.seat.controller;

import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.dto.SeatUpdateRequest;
import com.concertmania.ticketing.utils.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Seat Management", description = "좌석 관리 관련 API")
public interface SeatControllerDocs {

    @Operation(
            summary = "좌석 배치 생성",
            description = "여러 좌석을 한 번에 생성합니다. 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "좌석 배치 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "좌석 배치 생성 성공 예시",
                                    value = """
                                    [
                                        {
                                            "id": 1,
                                            "concertId": 1,
                                            "concertTitle": "IU 콘서트 2024",
                                            "section": "A",
                                            "row": "1",
                                            "number": "1",
                                            "grade": "VIP",
                                            "price": 150000.00,
                                            "isReserved": false,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00"
                                        },
                                        {
                                            "id": 2,
                                            "concertId": 1,
                                            "concertTitle": "IU 콘서트 2024",
                                            "section": "A",
                                            "row": "1",
                                            "number": "2",
                                            "grade": "VIP",
                                            "price": 150000.00,
                                            "isReserved": false,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00"
                                        }
                                    ]
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
                                        "message": "구역은 필수입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
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
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "비즈니스 로직 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "콘서트 없음",
                                            value = """
                                            {
                                                "status": 422,
                                                "message": "콘서트를 찾을 수 없습니다."
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "좌석 중복",
                                            value = """
                                            {
                                                "status": 422,
                                                "message": "이미 존재하는 좌석입니다."
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "요청 내 중복",
                                            value = """
                                            {
                                                "status": 422,
                                                "message": "요청 내에 중복된 좌석이 있습니다."
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<List<SeatResponse>> createSeats(
            @Parameter(
                    description = "좌석 생성 요청 목록",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "좌석 배치 생성 요청 예시",
                                    value = """
                                    [
                                        {
                                            "concertId": 1,
                                            "section": "A",
                                            "row": "1",
                                            "number": "1",
                                            "grade": "VIP",
                                            "price": 150000.00
                                        },
                                        {
                                            "concertId": 1,
                                            "section": "A",
                                            "row": "1",
                                            "number": "2",
                                            "grade": "VIP",
                                            "price": 150000.00
                                        }
                                    ]
                                    """
                            )
                    )
            )
            @Valid @RequestBody List<SeatCreateRequest> requests
    );

    @Operation(
            summary = "콘서트별 좌석 목록 조회",
            description = "특정 콘서트의 좌석 목록을 페이징으로 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "좌석 목록 조회 성공 예시",
                                    value = """
                                    {
                                        "content": [
                                            {
                                                "id": 1,
                                                "concertId": 1,
                                                "concertTitle": "IU 콘서트 2024",
                                                "section": "A",
                                                "row": "1",
                                                "number": "1",
                                                "grade": "VIP",
                                                "price": 150000.00,
                                                "isReserved": false,
                                                "createdAt": "2024-01-01T10:00:00",
                                                "updatedAt": "2024-01-01T10:00:00"
                                            }
                                        ],
                                        "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 20
                                        },
                                        "totalElements": 100,
                                        "totalPages": 5
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Page<SeatResponse>> getSeatsByConcert(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long concertId,
            
            @Parameter(description = "페이징 정보 (page, size, sort)")
            Pageable pageable
    );

    @Operation(
            summary = "콘서트 좌석 배치도 조회",
            description = "특정 콘서트의 전체 좌석 배치도를 조회합니다. 구역-행-번호 순으로 정렬됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 배치도 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "좌석 배치도 조회 성공 예시",
                                    value = """
                                    [
                                        {
                                            "id": 1,
                                            "concertId": 1,
                                            "concertTitle": "IU 콘서트 2024",
                                            "section": "A",
                                            "row": "1",
                                            "number": "1",
                                            "grade": "VIP",
                                            "price": 150000.00,
                                            "isReserved": false,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00"
                                        },
                                        {
                                            "id": 2,
                                            "concertId": 1,
                                            "concertTitle": "IU 콘서트 2024",
                                            "section": "A",
                                            "row": "1",
                                            "number": "2",
                                            "grade": "VIP",
                                            "price": 150000.00,
                                            "isReserved": true,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00"
                                        }
                                    ]
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<List<SeatResponse>> getSeatSeatMapByConcert(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long concertId
    );

    @Operation(
            summary = "등급별 좌석 조회",
            description = "특정 콘서트의 특정 등급 좌석만 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "등급별 좌석 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VIP 좌석 조회 성공 예시",
                                    value = """
                                    [
                                        {
                                            "id": 1,
                                            "concertId": 1,
                                            "concertTitle": "IU 콘서트 2024",
                                            "section": "A",
                                            "row": "1",
                                            "number": "1",
                                            "grade": "VIP",
                                            "price": 150000.00,
                                            "isReserved": false,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00"
                                        }
                                    ]
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<List<SeatResponse>> getSeatsByGrade(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long concertId,
            
            @Parameter(description = "좌석 등급", example = "VIP", required = true)
            @RequestParam String grade
    );

    @Operation(
            summary = "좌석 상세 조회",
            description = "좌석 ID로 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeatResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "좌석을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "좌석 없음",
                                    value = """
                                    {
                                        "status": 422,
                                        "message": "좌석을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SeatResponse> getSeat(
            @Parameter(description = "좌석 ID", example = "1", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "좌석 수정",
            description = "좌석 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeatResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
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
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "좌석을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SeatResponse> updateSeat(
            @Parameter(description = "좌석 ID", example = "1", required = true)
            @PathVariable Long id,
            
            @Parameter(
                    description = "좌석 수정 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeatUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "좌석 수정 요청 예시",
                                    value = """
                                    {
                                        "section": "A",
                                        "row": "2",
                                        "number": "1",
                                        "grade": "R",
                                        "price": 120000.00
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody SeatUpdateRequest request
    );

    @Operation(
            summary = "좌석 삭제",
            description = "좌석을 삭제합니다 (소프트 삭제). 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "좌석 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
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
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "비즈니스 로직 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "좌석 없음",
                                            value = """
                                            {
                                                "status": 422,
                                                "message": "좌석을 찾을 수 없습니다."
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "예약된 좌석",
                                            value = """
                                            {
                                                "status": 422,
                                                "message": "예약된 좌석은 삭제할 수 없습니다."
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> deleteSeat(
            @Parameter(description = "좌석 ID", example = "1", required = true)
            @PathVariable Long id
    );
}