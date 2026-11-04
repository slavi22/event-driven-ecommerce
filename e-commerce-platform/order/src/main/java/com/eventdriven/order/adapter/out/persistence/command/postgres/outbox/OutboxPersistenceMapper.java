package com.eventdriven.order.adapter.out.persistence.command.postgres.outbox;

import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface OutboxPersistenceMapper {
    OutboxEventEntity toOutboxEventEntity(OutboxEvent outboxEvent);
}
