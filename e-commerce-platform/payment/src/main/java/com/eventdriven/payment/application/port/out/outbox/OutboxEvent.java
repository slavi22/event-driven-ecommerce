package com.eventdriven.payment.application.port.out.outbox;

import java.time.Instant;

public record OutboxEvent(String aggregateId,
                          String aggregateType,
                          String eventType,
                          String payload,
                          Instant createdAt) {}
