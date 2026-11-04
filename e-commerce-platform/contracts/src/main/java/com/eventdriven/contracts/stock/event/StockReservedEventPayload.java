package com.eventdriven.contracts.stock.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record StockReservedEventPayload(
        String stockId,
        String productId,
        String orderId,
        int reservedAmount,
        int remainingQuantity,
        Instant occurredOn
) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Stock";
    public static final String EVENT_TYPE = "StockReserved";
}
