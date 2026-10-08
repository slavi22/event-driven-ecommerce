package com.eventdriven.notification.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedNotificationCommand(UUID orderId, BigDecimal amount) {
}
