package com.eventdriven.projection.order.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Read model representing an order")
public record GetOrderResponse(
        @Schema(description = "UUID of the order", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
        UUID orderId,

        @Schema(description = "UUID of the customer who placed the order", example = "c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f")
        UUID customerId,

        @Schema(description = "Total monetary amount of the order in USD", example = "299.98")
        BigDecimal totalAmount,

        @Schema(description = "Current status of the order", example = "CONFIRMED",
                allowableValues = {"PENDING", "CONFIRMED", "CANCELLED"})
        String status,

        @Schema(description = "Timestamp when the order was created", example = "2026-06-05T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the order was last updated", example = "2026-06-05T10:35:00Z")
        Instant updatedAt,

        @Schema(description = "Items included in the order")
        List<OrderItemResponse> items) {
}
