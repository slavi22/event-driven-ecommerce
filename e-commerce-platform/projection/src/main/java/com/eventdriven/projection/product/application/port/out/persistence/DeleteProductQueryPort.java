package com.eventdriven.projection.product.application.port.out.persistence;

import java.util.UUID;

public interface DeleteProductQueryPort {
    void deleteById(UUID productId);
}
