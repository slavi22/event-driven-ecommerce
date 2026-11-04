package com.eventdriven.stock.application.port.out.idempotency;

public interface IsEventProcessedPort {
    boolean isProcessed(String aggregateId, String eventType);
}
