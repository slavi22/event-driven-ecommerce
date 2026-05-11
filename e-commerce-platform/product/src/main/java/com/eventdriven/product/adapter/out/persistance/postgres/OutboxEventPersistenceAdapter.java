package com.eventdriven.product.adapter.out.persistance.postgres;

import com.eventdriven.product.application.dto.OutboxEvent;
import com.eventdriven.product.application.port.out.persistence.SaveOutboxEventPort;
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
