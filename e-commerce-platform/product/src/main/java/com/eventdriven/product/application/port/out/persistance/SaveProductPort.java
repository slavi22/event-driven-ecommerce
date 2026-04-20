package com.eventdriven.product.application.port.out.persistance;

import com.eventdriven.product.domain.entity.Product;

public interface SaveProductPort {
    Product saveProduct(Product product);
}
