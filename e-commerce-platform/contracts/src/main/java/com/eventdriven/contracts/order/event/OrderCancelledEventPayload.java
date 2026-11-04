package com.eventdriven.contracts.order.event;

import domain.event.DomainEvent;

import java.time.Instant;
import java.util.List;

// requiresStockRelease = true only from onPaymentFailed (stock was reserved and must be released)
// requiresStockRelease = false from onStockReservationFailed (nothing was reserved, no rollback needed)
public record OrderCancelledEventPayload(
        String orderId,
        List<OrderItemPayload> items,
        boolean requiresStockRelease,
        String reason,
        Instant occurredOn) implements DomainEvent {
    public static final String AGGREGATE_TYPE = "Order";
    public static final String EVENT_TYPE = "OrderCancelled";
}