package com.eventdriven.projection.order.application.dto;

import java.util.UUID;

public record OrderItemResult(UUID productId, String productName, int quantity) {
}
