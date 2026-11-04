package com.eventdriven.stock.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after successfully replenishing stock")
public record ReplenishStockResponse(
        @Schema(description = "UUID of the product whose stock was replenished", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
        UUID productId,

        @Schema(description = "Updated total stock quantity after replenishment", example = "150")
        int newQuantity) {
}
