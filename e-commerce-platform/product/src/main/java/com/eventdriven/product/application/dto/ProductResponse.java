package com.eventdriven.product.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(String name,
                              String description,
                              BigDecimal price,
                              String category,
                              String status,
                              Instant createdAt) {
}
