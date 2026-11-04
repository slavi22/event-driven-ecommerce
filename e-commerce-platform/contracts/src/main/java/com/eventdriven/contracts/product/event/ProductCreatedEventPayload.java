package com.eventdriven.contracts.product.event;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductCreatedEventPayload(String productId,
                                         String name,
                                         String description,
                                         BigDecimal price,
                                         ProductCategory category,
                                         ProductStatus status,
                                         int initialQuantity,
                                         Instant occurredOn) implements DomainEvent {

    public static final String AGGREGATE_TYPE = "Product";
    public static final String EVENT_TYPE = "ProductCreated";
}
