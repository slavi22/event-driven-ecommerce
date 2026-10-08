package com.eventdriven.order.adapter.out.persistence.command.postgres.outbox;

import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OutboxEventPersistenceAdapter implements SaveOutboxEventPort {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxPersistenceMapper outboxPersistenceMapper;

    @Override
    public void save(OutboxEvent event) {
        outboxEventJpaRepository.save(outboxPersistenceMapper.toOutboxEventEntity(event));
    }
}
