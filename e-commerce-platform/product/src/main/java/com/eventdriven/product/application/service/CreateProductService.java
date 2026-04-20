package com.eventdriven.product.application.service;

import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.application.mapper.ProductMapper;
import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.application.port.out.persistance.SaveProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Log4j2
@Service
@Validated
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {
    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        Product newProduct = Product.create(
                command.name(),
                command.description(),
                Money.of(command.price()),
                command.category()
        );

        saveProductPort.saveProduct(newProduct);
        // save to outbox table later

        return productMapper.toProductResponse(newProduct);
    }
}
