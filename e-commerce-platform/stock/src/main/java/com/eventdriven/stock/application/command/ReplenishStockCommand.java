package com.eventdriven.stock.application.command;

import java.util.UUID;

public record ReplenishStockCommand(UUID productId, int amount) {
}
