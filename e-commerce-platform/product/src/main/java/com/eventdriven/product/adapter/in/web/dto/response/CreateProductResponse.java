package com.eventdriven.product.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Schema(description = "Response returned after successfully creating a product")
public record CreateProductResponse(
        @Schema(description = "Product name", example = "Wireless Headphones")
        String name,

        @Schema(description = "Product description", example = "Premium noise-cancelling wireless headphones with 30-hour battery life")
        String description,

        @Schema(description = "Product price in USD", example = "149.99")
        BigDecimal price,

        @Schema(description = "Product category", example = "Electronics")
        String category,

        @Schema(description = "Current product status", example = "ACTIVE")
        String status,

        @Schema(description = "Timestamp when the product was created", example = "2026-06-05T10:30:00Z")
        ZonedDateTime createdAt) {
}
