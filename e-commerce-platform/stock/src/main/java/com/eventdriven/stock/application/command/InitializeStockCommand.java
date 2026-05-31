package com.eventdriven.stock.application.command;

import java.util.UUID;

public record InitializeStockCommand(UUID productId, int initialQuantity) {
}
