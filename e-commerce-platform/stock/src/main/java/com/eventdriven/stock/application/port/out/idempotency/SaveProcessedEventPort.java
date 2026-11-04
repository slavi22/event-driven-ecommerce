package com.eventdriven.stock.application.port.out.idempotency;

public interface SaveProcessedEventPort {
    void save(String aggregateId, String eventType);
}
