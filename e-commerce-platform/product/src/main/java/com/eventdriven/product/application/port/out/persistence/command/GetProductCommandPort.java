package com.eventdriven.product.application.port.out.persistence.command;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;

import java.util.Optional;

public interface GetProductCommandPort {
    Optional<Product> getProductByProductId(ProductId productId);
}
