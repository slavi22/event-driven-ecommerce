package com.eventdriven.projection.stock.application.dto;

import java.time.Instant;
import java.util.UUID;

public record GetStockResult(UUID stockId, UUID productId, int quantity, Instant createdAt, Instant updatedAt) {
}
