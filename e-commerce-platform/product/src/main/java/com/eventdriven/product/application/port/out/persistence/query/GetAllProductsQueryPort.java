package com.eventdriven.product.application.port.out.persistence.query;

import com.eventdriven.product.domain.entity.Product;

import java.util.List;

public interface GetAllProductsQueryPort {
    List<Product> getAllProducts();
}
