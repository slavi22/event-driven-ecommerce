package com.eventdriven.contracts.order.event;

public record OrderItemPayload(String productId, int quantity) {
}
