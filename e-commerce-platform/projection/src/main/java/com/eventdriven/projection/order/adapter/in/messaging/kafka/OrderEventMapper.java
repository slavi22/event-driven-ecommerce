package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.projection.order.application.dto.GetOrderResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEventMapper {

    default GetOrderResult toGetOrderResult(OrderPlacedEventPayload payload) {
        return new GetOrderResult(
                UUID.fromString(payload.orderId()),
                UUID.fromString(payload.customerId()),
                payload.totalAmount(),
                "PENDING",
                payload.occurredOn(),
                null
        );
    }
}
