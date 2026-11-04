package com.eventdriven.order.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request body to place a new order")
public record PlaceOrderRequest(
        @Schema(description = "List of items to order (at least one required)")
        @NotNull(message = "Items must not be null")
        @NotEmpty(message = "Order must have at least one item")
        //@Valid // TODO: check if i need that
        List<OrderItemRequest> items) {
}
