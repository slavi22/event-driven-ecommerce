package com.eventdriven.projection.order.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "An item within an order")
public record OrderItemResponse(
        @Schema(description = "UUID of the product", example = "d1e2f3a4-5b6c-7d8e-9f0a-1b2c3d4e5f6a")
        UUID productId,

        @Schema(description = "Name of the product", example = "Wireless Headphones")
        String productName,

        @Schema(description = "Quantity ordered", example = "2")
        int quantity) {
}
