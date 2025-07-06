package com.concertmania.ticketing.notification;

import com.concertmania.ticketing.notification.dto.EmailNotificationMessage;
import com.concertmania.ticketing.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailNotificationConsumer {
    private final NotificationService notificaitonService;

    @RabbitListener(queues = "notification.email.queue")
    public void consume(EmailNotificationMessage message) {
        log.info("[알림 수신] 이메일 발송 시작 -> {}", message.getEmail());
        notificaitonService.send(message.getEmail(), message.getSubject(), message.getContent());
    }
}
