package com.eventdriven.stock.adapter.in.web.dto.response;

import java.util.UUID;

public record ReplenishStockResponse(UUID productId, int newQuantity) {
}
