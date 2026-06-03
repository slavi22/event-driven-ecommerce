package com.eventdriven.order.application.port.out.outbox;

public interface SaveOutboxEventPort {

    void save(OutboxEvent event);
}
