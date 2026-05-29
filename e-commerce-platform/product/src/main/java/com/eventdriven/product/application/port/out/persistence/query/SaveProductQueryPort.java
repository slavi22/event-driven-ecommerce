package com.eventdriven.product.application.port.out.persistence.query;

import com.eventdriven.product.domain.entity.Product;

public interface SaveProductQueryPort {
    Product save(Product product);
}
