package com.eventdriven.product.application.port.out.persistence.outbox;

import java.time.Instant;

public record OutboxEvent(String aggregateId,
                          String aggregateType,
                          String eventType,
                          String payload,
                          Instant createdAt) {
}
