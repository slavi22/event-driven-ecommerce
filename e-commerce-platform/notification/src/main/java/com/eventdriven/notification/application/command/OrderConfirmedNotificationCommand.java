package com.eventdriven.notification.application.command;

import java.util.UUID;

public record OrderConfirmedNotificationCommand(UUID orderId) {
}
