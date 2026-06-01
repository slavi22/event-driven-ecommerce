package com.eventdriven.projection.product.application.dto;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;

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
