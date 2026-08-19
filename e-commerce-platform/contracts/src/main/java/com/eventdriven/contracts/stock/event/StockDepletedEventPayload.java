package com.eventdriven.contracts.stock.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record StockDepletedEventPayload(
        String stockId,
        String productId,
        Instant occurredOn
) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Stock";
    public static final String EVENT_TYPE = "StockDepleted";
}
