package com.concertmania.ticketing.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotificationMessage {
    private String email;
    private String subject;
    private String content;
}