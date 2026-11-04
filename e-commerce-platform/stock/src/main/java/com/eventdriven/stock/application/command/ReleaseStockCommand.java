package com.eventdriven.stock.application.command;

import java.util.UUID;

public record ReleaseStockCommand(UUID orderId, UUID productId, int amount) {
}
