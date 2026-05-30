package com.eventdriven.contracts.product.event;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;

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
