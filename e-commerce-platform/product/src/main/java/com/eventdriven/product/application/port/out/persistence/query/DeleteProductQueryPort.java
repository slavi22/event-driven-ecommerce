package com.eventdriven.product.application.port.out.persistence.query;

import com.eventdriven.product.domain.valueobject.ProductId;

public interface DeleteProductQueryPort {
    void deleteProductById(ProductId productId);
}
