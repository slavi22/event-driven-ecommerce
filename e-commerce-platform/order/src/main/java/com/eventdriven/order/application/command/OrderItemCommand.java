package com.eventdriven.order.application.command;

import java.util.UUID;

public record OrderItemCommand(UUID productId, int quantity) {
}
