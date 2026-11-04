package com.eventdriven.stock.application.port.out.outbox;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);
}
