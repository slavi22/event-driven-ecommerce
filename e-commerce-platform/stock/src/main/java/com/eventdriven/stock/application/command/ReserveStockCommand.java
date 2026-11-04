package com.eventdriven.stock.application.command;

import java.util.UUID;

public record ReserveStockCommand(UUID orderId, UUID productId, int amount) {
}
