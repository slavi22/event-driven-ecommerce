package com.eventdriven.stock.adapter.out.persistence.command.postgres.idempotency;

import com.eventdriven.stock.application.port.out.idempotency.IsEventProcessedPort;
import com.eventdriven.stock.application.port.out.idempotency.SaveProcessedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
class ProcessedEventPersistenceAdapter implements IsEventProcessedPort, SaveProcessedEventPort {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    @Override
    public boolean isProcessed(String aggregateId, String eventType) {
        return processedEventJpaRepository.existsByAggregateIdAndEventType(aggregateId, eventType);
    }

    @Override
    public void save(String aggregateId, String eventType) {
        ProcessedEventEntity entity = new ProcessedEventEntity();
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setProcessedAt(Instant.now());
        processedEventJpaRepository.save(entity);
    }
}
