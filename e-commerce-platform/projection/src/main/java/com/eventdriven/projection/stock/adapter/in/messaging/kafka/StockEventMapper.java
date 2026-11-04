package com.eventdriven.projection.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.contracts.stock.event.StockReplenishedEventPayload;
import com.eventdriven.projection.stock.application.dto.GetStockResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockEventMapper {

    default GetStockResult toGetStockResult(StockInitializedEventPayload payload) {
        return new GetStockResult(
                UUID.fromString(payload.stockId()),
                UUID.fromString(payload.productId()),
                payload.quantity(),
                payload.occurredOn(),
                null
        );
    }

    default GetStockResult toGetStockResult(StockReplenishedEventPayload payload) {
        return new GetStockResult(
                UUID.fromString(payload.stockId()),
                UUID.fromString(payload.productId()),
                payload.newQuantity(),
                null,
                payload.occurredOn()
        );
    }
}
