package com.concertmania.ticketing.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대기열 등록 응답")
public class QueueRegisterResponse {
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "대기열 순번", example = "150")
    private Long position;
    
    @Schema(description = "등록 여부", example = "true")
    private boolean isRegister;
    
    @Schema(description = "응답 메시지", example = "대기열에 등록되었습니다.")
    private String message;
}
