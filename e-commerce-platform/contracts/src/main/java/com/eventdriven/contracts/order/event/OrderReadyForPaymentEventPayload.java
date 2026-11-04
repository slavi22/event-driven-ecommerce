package com.eventdriven.contracts.order.event;

import domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderReadyForPaymentEventPayload(
        String orderId,
        BigDecimal amount,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Order";
    public static final String EVENT_TYPE = "OrderReadyForPayment";
}
