package com.eventdriven.product.domain.event;

import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductCreatedEventPayload(String productId,
                                         String name,
                                         String description,
                                         BigDecimal price,
                                         ProductCategory category,
                                         ProductStatus status,
                                         Instant occurredOn) implements DomainEvent {

    public static final String AGGREGATE_TYPE = "Product";
    public static final String EVENT_TYPE = "ProductCreated";
}
