package com.eventdriven.notification.application.command;

import java.util.UUID;

public record OrderCancelledNotificationCommand(UUID orderId, String reason) {
}
