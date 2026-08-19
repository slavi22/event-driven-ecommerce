package com.eventdriven.projection.product.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.contracts.product.event.ProductUpdatedEventPayload;
import com.eventdriven.projection.product.application.dto.GetProductResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductEventMapper {

    default GetProductResult toGetProductResult(ProductCreatedEventPayload payload) {
        return new GetProductResult(
                payload.productId(),
                payload.name(),
                payload.description(),
                payload.price(),
                payload.category(),
                payload.status(),
                payload.occurredOn(),
                null
        );
    }

    default GetProductResult toGetProductResult(ProductUpdatedEventPayload payload) {
        return new GetProductResult(
                payload.productId(),
                payload.name(),
                payload.description(),
                payload.price(),
                payload.category(),
                payload.status(),
                payload.createdOn(),
                payload.updatedOn()
        );
    }
}
