package com.eventdriven.order.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "A single item within an order")
public record OrderItemRequest(
        @Schema(description = "UUID of the product to order", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
        @NotNull(message = "Product ID must not be null")
        UUID productId,

        @Schema(description = "Quantity of the product (must be at least 1)", example = "2")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity) {
}
