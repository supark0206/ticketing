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
@Schema(description = "대기열 상태 응답")
public class QueueStatusResponse {
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "대기열 순번", example = "150")
    private Integer position;
    
    @Schema(description = "전체 대기열 수", example = "500")
    private Integer totalQueue;
    
    @Schema(description = "예상 대기시간(초)", example = "300")
    private Integer estimatedWaitTime;
    
    @Schema(description = "입장 가능 여부", example = "false")
    private Boolean canEnter;
    
    @Schema(description = "타임스탬프", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;
}
