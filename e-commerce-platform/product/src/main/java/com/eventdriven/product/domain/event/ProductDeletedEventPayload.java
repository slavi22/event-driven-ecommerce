package com.eventdriven.product.domain.event;

import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDeletedEventPayload(String productId,
                                         String name,
                                         String description,
                                         BigDecimal amount,
                                         ProductCategory category,
                                         ProductStatus status,
                                         Instant createdAt,
                                         Instant updatedAt) {

    public static final String AGGREGATE_TYPE = "Product";
    public static final String EVENT_TYPE = "ProductDeleted";
}
