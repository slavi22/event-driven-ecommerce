package com.eventdriven.notification.application.port.out.model;

public record Notification(
        String recipientId,
        String subject,
        String body
) {
}
