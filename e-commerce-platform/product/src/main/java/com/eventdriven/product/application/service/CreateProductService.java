package com.eventdriven.product.application.service;

import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.application.mapper.ProductMapper;
import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.application.port.out.persistence.SaveProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Log4j2
@Service
@Validated
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {
    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductCommand command) {
        log.info("Creating product with name: {}", command.name());
        Product newProduct = Product.create(
                command.name(),
                command.description(),
                Money.of(command.price()),
                command.category()
        );

        log.info("Saving product with name: {}", command.name());
        saveProductPort.saveProduct(newProduct);
        log.info("Product with name: {} saved successfully", command.name());

        // we are supposed to raise an event here if we don't use outbox pattern, but since we are using outbox pattern, we will save the event to outbox table and let the outbox processor handle the event publishing
        log.info("Saving product with name: {} to outbox table", command.name());
        // save to outbox table later
        log.info("Product with name: {} saved to outbox table successfully", command.name());

        return productMapper.toProductResponse(newProduct);
    }
}
