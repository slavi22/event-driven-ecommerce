package com.eventdriven.product.adapter.out.persistence.command.postgres.outbox;

import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OutboxPersistenceMapper {
    OutboxEventEntity toOutboxEventEntity(OutboxEvent outboxEvent);
}
