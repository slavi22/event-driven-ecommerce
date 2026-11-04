package com.eventdriven.product.application.port.out.persistence.command;

import com.eventdriven.product.domain.entity.Product;

public interface UpdateProductPort {
    Product update(Product product);
}
