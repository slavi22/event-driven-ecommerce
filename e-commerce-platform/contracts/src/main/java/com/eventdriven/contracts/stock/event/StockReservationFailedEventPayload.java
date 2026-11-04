package com.eventdriven.contracts.stock.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record StockReservationFailedEventPayload(
        String orderId,
        String productId,
        String reason,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Stock";
    public static final String EVENT_TYPE = "StockReservationFailed";
}
