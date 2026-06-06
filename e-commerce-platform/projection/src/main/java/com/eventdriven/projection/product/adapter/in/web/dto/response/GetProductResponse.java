package com.eventdriven.projection.product.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Read model representing a product")
public record GetProductResponse(
        @Schema(description = "UUID of the product", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
        String productId,

        @Schema(description = "Product name", example = "Wireless Headphones")
        String name,

        @Schema(description = "Product description", example = "Premium noise-cancelling wireless headphones with 30-hour battery life")
        String description,

        @Schema(description = "Product price in USD", example = "149.99")
        BigDecimal price,

        @Schema(description = "Product category", example = "Electronics")
        String category,

        @Schema(description = "Current product status", example = "ACTIVE",
                allowableValues = {"ACTIVE", "DELETED"})
        String status,

        @Schema(description = "Timestamp when the product was created", example = "2026-06-05T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the product was last updated", example = "2026-06-05T10:35:00Z")
        Instant updatedAt) {
}
