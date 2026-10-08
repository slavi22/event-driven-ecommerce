package com.eventdriven.stock.adapter.out.persistence.command.postgres.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, ProcessedEventId> {
    boolean existsByAggregateIdAndEventType(String aggregateId, String eventType);
}
