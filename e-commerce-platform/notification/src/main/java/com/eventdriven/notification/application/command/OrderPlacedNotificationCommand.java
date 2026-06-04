package com.eventdriven.notification.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedNotificationCommand(UUID orderId, UUID customerId, BigDecimal totalAmount) {
}
