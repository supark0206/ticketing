package com.concertmania.ticketing.concert.controller;

import com.concertmania.ticketing.concert.dto.ConcertCreateRequest;
import com.concertmania.ticketing.concert.dto.ConcertResponse;
import com.concertmania.ticketing.concert.dto.ConcertUpdateRequest;
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

@Tag(name = "Concert Management", description = "콘서트 관리 관련 API")
public interface ConcertControllerDocs {

    @Operation(
            summary = "콘서트 생성",
            description = "새로운 콘서트를 생성합니다. 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "콘서트 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConcertResponse.class),
                            examples = @ExampleObject(
                                    name = "콘서트 생성 성공 예시",
                                    value = """
                                    {
                                        "id": 1,
                                        "title": "IU 콘서트 2024",
                                        "concertDate": "2024-12-31T19:00:00",
                                        "venue": "올림픽공원 체조경기장",
                                        "openTime": "2024-12-01T10:00:00",
                                        "closeTime": "2024-12-30T23:59:59",
                                        "status": "SCHEDULED",
                                        "createdAt": "2024-01-01T10:00:00",
                                        "updatedAt": "2024-01-01T10:00:00"
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
                                        "message": "콘서트 제목은 필수입니다."
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
            )
    })
    ResponseEntity<ConcertResponse> createConcert(
            @Parameter(
                    description = "콘서트 생성 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConcertCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "콘서트 생성 요청 예시",
                                    value = """
                                    {
                                        "title": "IU 콘서트 2024",
                                        "venue": "올림픽공원 체조경기장",
                                        "concertDate": "2024-12-31T19:00:00",
                                        "openTime": "2024-12-01T10:00:00",
                                        "closeTime": "2024-12-30T23:59:59"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody ConcertCreateRequest request
    );

    @Operation(
            summary = "콘서트 목록 조회",
            description = "콘서트 목록을 조회합니다. 제목과 장소로 검색할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "콘서트 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "콘서트 목록 조회 성공 예시",
                                    value = """
                                    {
                                        "content": [
                                            {
                                                "id": 1,
                                                "title": "IU 콘서트 2024",
                                                "concertDate": "2024-12-31T19:00:00",
                                                "venue": "올림픽공원 체조경기장",
                                                "openTime": "2024-12-01T10:00:00",
                                                "closeTime": "2024-12-30T23:59:59",
                                                "status": "SCHEDULED",
                                                "createdAt": "2024-01-01T10:00:00",
                                                "updatedAt": "2024-01-01T10:00:00"
                                            }
                                        ],
                                        "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 20
                                        },
                                        "totalElements": 1,
                                        "totalPages": 1
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<Page<ConcertResponse>> getAllConcerts(
            @Parameter(description = "콘서트 제목 검색어 (부분 검색)", example = "IU")
            @RequestParam(required = false) String title,
            
            @Parameter(description = "콘서트 장소 검색어 (부분 검색)", example = "올림픽")
            @RequestParam(required = false) String venue,
            
            @Parameter(description = "페이징 정보 (page, size, sort)")
            Pageable pageable
    );

    @Operation(
            summary = "콘서트 상세 조회",
            description = "콘서트 ID로 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "콘서트 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConcertResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "콘서트 없음",
                                    value = """
                                    {
                                        "status": 422,
                                        "message": "콘서트를 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<ConcertResponse> getConcert(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "콘서트 수정",
            description = "콘서트 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "콘서트 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConcertResponse.class)
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
                    responseCode = "404",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ConcertResponse> updateConcert(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long id,
            
            @Parameter(
                    description = "콘서트 수정 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConcertUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "콘서트 수정 요청 예시",
                                    value = """
                                    {
                                        "title": "IU 콘서트 2024 (수정됨)",
                                        "venue": "잠실실내체육관",
                                        "concertDate": "2024-12-31T19:00:00",
                                        "openTime": "2024-12-01T10:00:00",
                                        "closeTime": "2024-12-30T23:59:59"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody ConcertUpdateRequest request
    );

    @Operation(
            summary = "콘서트 삭제",
            description = "콘서트를 삭제합니다 (소프트 삭제). 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "콘서트 삭제 성공"
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
                    responseCode = "404",
                    description = "콘서트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteConcert(
            @Parameter(description = "콘서트 ID", example = "1", required = true)
            @PathVariable Long id
    );
}