package com.eventdriven.product.application.dto;

import java.time.Instant;

public record OutboxEvent(String aggregateId,
                          String aggregateType,
                          String eventType,
                          String payload,
                          Instant createdAt) {
}
