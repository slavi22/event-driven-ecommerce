package com.eventdriven.product.application.port.out.persistence.outbox;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);
}
