package com.eventdriven.product.application.port.out.persistence.query;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;

import java.util.Optional;

public interface GetProductQueryPort {
    Optional<Product> getProductByProductId(ProductId productId);
}
