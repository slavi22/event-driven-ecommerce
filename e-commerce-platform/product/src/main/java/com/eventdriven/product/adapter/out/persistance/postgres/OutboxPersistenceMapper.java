package com.eventdriven.product.adapter.out.persistance.postgres;

import com.eventdriven.product.application.dto.OutboxEvent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OutboxPersistenceMapper {
    OutboxEventEntity toOutboxEventEntity(OutboxEvent outboxEvent);
}
