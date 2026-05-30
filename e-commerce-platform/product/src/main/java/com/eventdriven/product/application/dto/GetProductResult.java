package com.eventdriven.product.application.dto;

import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record GetProductResult(String productId,
                               String name,
                               String description,
                               BigDecimal price,
                               ProductCategory category,
                               ProductStatus status,
                               Instant createdAt,
                               Instant updatedAt) {
}
