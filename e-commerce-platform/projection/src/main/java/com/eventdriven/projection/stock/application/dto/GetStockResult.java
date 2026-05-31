package com.eventdriven.projection.stock.application.dto;

import java.util.UUID;

public record GetStockResult(UUID productId, int quantity) {
}
