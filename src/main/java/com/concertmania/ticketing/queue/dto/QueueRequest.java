package com.concertmania.ticketing.queue.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대기열 등록 요청")
public class QueueRequest {

    @NotNull(message = "콘서트 선택은 필수입니다.")
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;
}
