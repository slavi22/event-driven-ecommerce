package com.eventdriven.contracts.order.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record OrderConfirmedEventPayload(
        String orderId, Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Order";
    public static final String EVENT_TYPE = "OrderConfirmed";
}
