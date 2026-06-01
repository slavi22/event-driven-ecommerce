package com.eventdriven.stock.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReplenishStockRequest(
        @NotNull(message = "Amount must not be null")
        @Min(value = 1, message = "Amount must be at least 1")
        Integer amount) {
}
