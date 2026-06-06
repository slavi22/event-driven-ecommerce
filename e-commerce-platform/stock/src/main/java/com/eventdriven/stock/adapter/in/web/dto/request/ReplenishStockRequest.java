package com.eventdriven.stock.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to replenish stock for a product")
public record ReplenishStockRequest(
        @Schema(description = "Number of units to add to existing stock (must be at least 1)", example = "50")
        @NotNull(message = "Amount must not be null")
        @Min(value = 1, message = "Amount must be at least 1")
        Integer amount) {
}
