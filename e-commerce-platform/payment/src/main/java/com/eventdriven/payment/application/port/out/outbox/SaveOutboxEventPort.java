package com.eventdriven.payment.application.port.out.outbox;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);
}
