package com.eventdriven.order.adapter.in.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(
        @NotNull(message = "Items must not be null")
        @NotEmpty(message = "Order must have at least one item")
        //@Valid // TODO: check if i need that
        List<OrderItemRequest> items) {
}
