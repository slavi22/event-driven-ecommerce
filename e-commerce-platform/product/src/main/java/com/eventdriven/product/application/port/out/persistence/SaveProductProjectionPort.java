package com.eventdriven.product.application.port.out.persistence;

import com.eventdriven.product.domain.entity.Product;

public interface SaveProductProjectionPort {
    Product save(Product product);
}
