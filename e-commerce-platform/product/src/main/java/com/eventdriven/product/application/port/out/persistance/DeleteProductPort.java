package com.eventdriven.product.application.port.out.persistance;

import com.eventdriven.product.domain.valueobject.ProductId;

public interface DeleteProductPort {
    void deleteProductById(ProductId productId);
}
