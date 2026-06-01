package com.eventdriven.projection.stock.adapter.in.web.dto.response;

import java.util.UUID;

public record GetStockResponse(UUID productId, int quantity) {
}
