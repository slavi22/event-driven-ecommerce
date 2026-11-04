package com.eventdriven.product.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response returned after successfully updating a product")
public record UpdateProductResponse(
        @Schema(description = "Updated product name", example = "Wireless Headphones Pro")
        String name,

        @Schema(description = "Updated product description", example = "Premium noise-cancelling headphones with 40-hour battery life")
        String description,

        @Schema(description = "Updated product price in USD", example = "179.99")
        BigDecimal price,

        @Schema(description = "Updated product category", example = "Electronics")
        String category,

        @Schema(description = "Current product status", example = "ACTIVE")
        String status) {
}
