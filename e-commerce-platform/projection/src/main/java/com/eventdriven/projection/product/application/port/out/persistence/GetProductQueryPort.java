package com.eventdriven.projection.product.application.port.out.persistence;

import com.eventdriven.projection.product.application.dto.GetProductResult;

import java.util.Optional;
import java.util.UUID;

public interface GetProductQueryPort {
    Optional<GetProductResult> getProductById(UUID productId);
}
