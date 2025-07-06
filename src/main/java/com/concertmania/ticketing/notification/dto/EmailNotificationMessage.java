package com.concertmania.ticketing.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이메일 알림 메시지")
public class EmailNotificationMessage {
    @Schema(description = "수신자 이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "이메일 제목", example = "[ConcertMania] 예매 완료 안내")
    private String subject;
    
    @Schema(description = "이메일 내용", example = "안녕하세요. 콘서트 예매가 완료되었습니다.")
    private String content;
}