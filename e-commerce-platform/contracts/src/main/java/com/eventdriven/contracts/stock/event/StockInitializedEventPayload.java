package com.eventdriven.contracts.stock.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record StockInitializedEventPayload(
        String stockId,
        String productId,
        int quantity,
        Instant occurredOn
) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Stock";
    public static final String EVENT_TYPE = "StockInitialized";
}
