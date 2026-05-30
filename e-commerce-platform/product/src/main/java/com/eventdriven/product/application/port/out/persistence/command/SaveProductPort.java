package com.eventdriven.product.application.port.out.persistence.command;

import com.eventdriven.product.domain.entity.Product;

public interface SaveProductPort {
    Product save(Product product);
}
