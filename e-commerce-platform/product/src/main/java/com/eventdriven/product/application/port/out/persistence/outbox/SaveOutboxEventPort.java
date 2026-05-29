package com.eventdriven.product.application.port.out.persistence.outbox;

import com.eventdriven.product.application.dto.OutboxEvent;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);
}
