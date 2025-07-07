package com.concertmania.ticketing.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private Long concertId;
    private Long userId;
    private Integer position;
    private Integer totalQueue;
    private Integer estimatedWaitTime; // 예상 대기시간(초)
    private Boolean canEnter; // 입장 가능 여부
    private LocalDateTime timestamp;
}
