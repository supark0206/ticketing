package com.concertmania.ticketing.user.docs;

import com.concertmania.ticketing.user.dto.LoginRequest;
import com.concertmania.ticketing.user.dto.SignupRequest;
import com.concertmania.ticketing.user.dto.UserResponse;
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
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "User Authentication", description = "사용자 관련 API")
public interface UserControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 성공 시 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    value = """
                                    {
                                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "type": "Bearer",
                                        "email": "user@example.com",
                                        "username": "testuser",
                                        "role": "USER"
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
                                        "message": "이메일 형식이 올바르지 않습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 이메일 또는 사용자명",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 중복",
                                    value = """
                                    {
                                        "status": 409,
                                        "message": "이미 존재하는 이메일입니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<UserResponse> signup(
            @Parameter(
                    description = "회원가입 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(
                                    name = "회원가입 요청 예시",
                                    value = """
                                    {
                                        "username": "testuser",
                                        "email": "user@example.com",
                                        "password": "password123"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody SignupRequest request
    );

    @Operation(
            summary = "로그인",
            description = "사용자 로그인을 수행합니다. 성공 시 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공 예시",
                                    value = """
                                    {
                                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "type": "Bearer",
                                        "email": "admin@concertmania.com",
                                        "username": "admin",
                                        "role": "ADMIN"
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
                                        "message": "이메일은 필수입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 잘못된 이메일 또는 비밀번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                    {
                                        "status": 401,
                                        "message": "이메일 또는 비밀번호가 잘못되었습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "사용자 없음",
                                    value = """
                                    {
                                        "status": 404,
                                        "message": "사용자를 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<UserResponse> login(
            @Parameter(
                    description = "로그인 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "일반 사용자 로그인",
                                            value = """
                                            {
                                                "email": "user@example.com",
                                                "password": "password123"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "관리자 로그인",
                                            value = """
                                            {
                                                "email": "admin@concertmania.com",
                                                "password": "1234"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody LoginRequest request
    );
}