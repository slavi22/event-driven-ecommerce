package com.eventdriven.notification.application.command;

import java.util.UUID;

public record PaymentFailedNotificationCommand(UUID orderId, String reason) {
}
