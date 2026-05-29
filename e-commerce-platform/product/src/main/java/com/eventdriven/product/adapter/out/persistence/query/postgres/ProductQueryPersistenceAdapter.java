package com.eventdriven.product.adapter.out.persistence.query.postgres;

import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.port.out.persistence.query.GetAllProductsQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.GetProductQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.SaveProductQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.UpdateProductQueryPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ProductQueryPersistenceAdapter implements GetProductQueryPort, GetAllProductsQueryPort, SaveProductQueryPort, UpdateProductQueryPort {

    private final ProductReadJpaRepository productReadJpaRepository;
    private final ProductQueryPersistenceMapper productQueryPersistenceMapper;

    @Override
    public List<Product> getAllProducts() {
        return productReadJpaRepository.findAll()
                                       .stream()
                                       .map(productQueryPersistenceMapper::toProductDomainEntity)
                                       .toList();
    }

    @Override
    public Optional<Product> getProductByProductId(ProductId productId) {
        return productReadJpaRepository.findById(productId.getValue())
                                       .map(productQueryPersistenceMapper::toProductDomainEntity);
    }

    @Override
    public Product save(Product product) {
        ProductReadEntity entity = productQueryPersistenceMapper.toProductReadEntity(product);
        return productQueryPersistenceMapper.toProductDomainEntity(productReadJpaRepository.save(entity));
    }

    @Override
    public Product update(Product product) {
        ProductReadEntity existingEntity = productReadJpaRepository.findById(product.getId().getValue())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with id " + product.getId().getValue() + " not found in projection!"));
        existingEntity.setName(product.getName());
        existingEntity.setDescription(product.getDescription());
        existingEntity.setPrice(product.getPrice().getAmount());
        existingEntity.setCategory(product.getCategory());
        existingEntity.setStatus(product.getStatus());
        existingEntity.setUpdatedAt(product.getUpdatedAt());
        return productQueryPersistenceMapper.toProductDomainEntity(productReadJpaRepository.save(existingEntity));
    }
}
