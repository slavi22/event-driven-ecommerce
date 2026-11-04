package com.eventdriven.projection.product.adapter.out.persistence.postgres;

import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.port.out.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProductQueryPersistenceAdapter implements GetProductQueryPort, GetAllProductsQueryPort,
        SaveProductQueryPort, UpdateProductQueryPort, DeleteProductQueryPort {

    private final ProductReadJpaRepository productReadJpaRepository;
    private final ProductQueryPersistenceMapper productQueryPersistenceMapper;

    @Override
    public Optional<GetProductResult> getProductById(UUID productId) {
        return productReadJpaRepository.findById(productId)
                                       .map(productQueryPersistenceMapper::toGetProductResult);
    }

    @Override
    public List<GetProductResult> getAllProducts() {
        return productReadJpaRepository.findAll()
                                       .stream()
                                       .map(productQueryPersistenceMapper::toGetProductResult)
                                       .toList();
    }

    @Override
    public GetProductResult save(GetProductResult product) {
        ProductReadEntity entity = productQueryPersistenceMapper.toProductReadEntity(product);
        return productQueryPersistenceMapper.toGetProductResult(productReadJpaRepository.save(entity));
    }

    @Override
    public GetProductResult update(GetProductResult product) {
        ProductReadEntity existing = productReadJpaRepository.findById(UUID.fromString(product.productId()))
                                                             .orElseThrow(() -> new IllegalStateException(
                                                                     "Product with id " + product.productId() +
                                                                     " not found in projection!"));
        existing.setName(product.name());
        existing.setDescription(product.description());
        existing.setPrice(product.price());
        existing.setCategory(product.category());
        existing.setStatus(product.status());
        existing.setUpdatedAt(product.updatedAt());
        return productQueryPersistenceMapper.toGetProductResult(productReadJpaRepository.save(existing));
    }

    @Override
    public void deleteById(UUID productId) {
        ProductReadEntity existing = productReadJpaRepository.findById(productId)
                                                             .orElseThrow(() -> new IllegalStateException(
                                                                     "Product with id " + productId +
                                                                     " not found in projection!"));
        existing.setStatus(ProductStatus.INACTIVE);
        productReadJpaRepository.save(existing);
    }
}
