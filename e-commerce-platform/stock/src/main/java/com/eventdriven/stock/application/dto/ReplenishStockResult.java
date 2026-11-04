package com.eventdriven.stock.application.dto;

import java.util.UUID;

public record ReplenishStockResult(UUID productId, int newQuantity) {
}
