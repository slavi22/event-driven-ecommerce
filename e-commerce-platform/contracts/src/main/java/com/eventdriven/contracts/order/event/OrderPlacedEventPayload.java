package com.eventdriven.contracts.order.event;

import domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPlacedEventPayload(
        String orderId,
        String customerId,
        List<OrderItemPayload> items,
        BigDecimal totalAmount,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Order";
    public static final String EVENT_TYPE = "OrderPlaced";
}
