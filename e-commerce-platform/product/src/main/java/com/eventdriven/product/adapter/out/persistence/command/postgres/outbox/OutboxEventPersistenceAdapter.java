package com.eventdriven.product.adapter.out.persistence.command.postgres.outbox;

import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements SaveOutboxEventPort {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxPersistenceMapper outboxPersistenceMapper;

    @Override
    public void save(OutboxEvent event) {
        outboxEventJpaRepository.save(outboxPersistenceMapper.toOutboxEventEntity(event));
    }
}
