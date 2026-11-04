package com.eventdriven.order.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after successfully placing an order")
public record PlaceOrderResponse(
        @Schema(description = "UUID of the newly created order", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
        UUID orderId) {
}
