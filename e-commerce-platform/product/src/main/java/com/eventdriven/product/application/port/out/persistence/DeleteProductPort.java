package com.eventdriven.product.application.port.out.persistence;

import com.eventdriven.product.domain.valueobject.ProductId;

public interface DeleteProductPort {
    void deleteProductById(ProductId productId);
}
