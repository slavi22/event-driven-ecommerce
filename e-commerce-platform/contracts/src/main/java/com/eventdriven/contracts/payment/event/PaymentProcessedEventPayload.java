package com.eventdriven.contracts.payment.event;

import domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessedEventPayload(
        String orderId,
        BigDecimal amount,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Payment";
    public static final String EVENT_TYPE = "PaymentProcessed";
}
