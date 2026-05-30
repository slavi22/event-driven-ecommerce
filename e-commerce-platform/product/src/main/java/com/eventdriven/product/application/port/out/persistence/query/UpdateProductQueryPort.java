package com.eventdriven.product.application.port.out.persistence.query;

import com.eventdriven.product.domain.entity.Product;

public interface UpdateProductQueryPort {
    Product update(Product product);
}
