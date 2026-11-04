package com.eventdriven.stock.adapter.out.persistence.command.postgres.idempotency;

import java.io.Serializable;
import java.util.Objects;

class ProcessedEventId implements Serializable {

    private String aggregateId;
    private String eventType;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedEventId that = (ProcessedEventId) o;
        return Objects.equals(aggregateId, that.aggregateId) &&
               Objects.equals(eventType, that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, eventType);
    }

}
