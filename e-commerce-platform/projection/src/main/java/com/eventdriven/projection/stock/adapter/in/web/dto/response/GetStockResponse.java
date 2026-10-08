package com.eventdriven.projection.stock.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Read model representing the current stock level for a product")
public record GetStockResponse(
        @Schema(description = "UUID of the product", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
        UUID productId,

        @Schema(description = "Current stock quantity available", example = "150")
        int quantity) {
}
