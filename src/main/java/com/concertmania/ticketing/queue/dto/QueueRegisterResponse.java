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
public class QueueRegisterResponse {
    private Long concertId;
    private Long userId;
    private Long position;
    private boolean isRegister;
    private String message;
}
