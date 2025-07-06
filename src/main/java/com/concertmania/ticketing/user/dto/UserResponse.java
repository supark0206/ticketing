package com.concertmania.ticketing.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 응답")
public class UserResponse {
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String type;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "사용자명", example = "홍길동")
    private String username;
    
    @Schema(description = "사용자 역할", example = "USER")
    private String role;
}