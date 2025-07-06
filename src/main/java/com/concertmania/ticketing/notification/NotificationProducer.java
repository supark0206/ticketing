package com.concertmania.ticketing.notification;

import com.concertmania.ticketing.notification.dto.EmailNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailNotification(String email, String subject, String content) {
        EmailNotificationMessage message = EmailNotificationMessage.builder()
                .email(email)
                .subject(subject)
                .content(content)
                .build();

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "notification.email",
                message
        );

        log.info("[알림 발행] 이메일 발송 메시지 전송 -> {}", email);
    }
}
