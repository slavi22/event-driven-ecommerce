package com.eventdriven.stock.adapter.out.persistence.command.postgres.idempotency;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "processed_events", schema = "stock")
@IdClass(ProcessedEventId.class)
class ProcessedEventEntity {

    @Id
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Id
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
