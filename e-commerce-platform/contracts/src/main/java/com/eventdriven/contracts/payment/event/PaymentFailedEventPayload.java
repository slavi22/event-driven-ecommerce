package com.eventdriven.contracts.payment.event;

import domain.event.DomainEvent;

import java.time.Instant;

public record PaymentFailedEventPayload(
        String orderId,
        String reason,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Payment";
    public static final String EVENT_TYPE = "PaymentFailed";
}
