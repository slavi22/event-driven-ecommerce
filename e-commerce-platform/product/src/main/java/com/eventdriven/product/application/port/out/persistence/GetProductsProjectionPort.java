package com.eventdriven.product.application.port.out.persistence;

import com.eventdriven.product.domain.entity.Product;

import java.util.List;

public interface GetProductsProjectionPort {
    List<Product> getProducts();
}
