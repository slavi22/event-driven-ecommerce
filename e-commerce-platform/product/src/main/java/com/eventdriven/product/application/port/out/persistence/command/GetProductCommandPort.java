package com.eventdriven.product.application.port.out.persistence.command;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;


public interface GetProductCommandPort {
    Product getProductByProductId(ProductId productId);
}
