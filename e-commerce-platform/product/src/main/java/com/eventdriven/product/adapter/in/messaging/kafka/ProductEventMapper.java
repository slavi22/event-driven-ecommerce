package com.eventdriven.product.adapter.in.messaging.kafka;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.contracts.product.event.ProductUpdatedEventPayload;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductEventMapper {

    default Product toProductDomainEntity(ProductCreatedEventPayload payload) {
        return Product.reconstitute(
                new ProductId(UUID.fromString(payload.productId())),
                payload.name(),
                payload.description(),
                Money.of(payload.price()),
                payload.category(),
                payload.status(),
                payload.occurredOn(),
                null // for created event, updatedAt is null, since the product has just been created and has not been updated yet.
        );
    }

    default Product toProductDomainEntity(ProductUpdatedEventPayload payload) {
        return Product.reconstitute(
                new ProductId(UUID.fromString(payload.productId())),
                payload.name(),
                payload.description(),
                Money.of(payload.price()),
                payload.category(),
                payload.status(),
                payload.createdOn(),
                payload.updatedOn()
        );
    }
}
