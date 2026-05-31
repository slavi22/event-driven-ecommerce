package com.eventdriven.product.adapter.out.persistence.command.postgres;

import com.eventdriven.product.application.port.out.persistence.command.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.command.SaveProductPort;
import com.eventdriven.product.application.port.out.persistence.command.UpdateProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import com.eventdriven.contracts.product.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
class ProductCommandPersistenceAdapter implements GetProductCommandPort, SaveProductPort, UpdateProductPort, DeleteProductPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductCommandPersistenceMapper productCommandPersistenceMapper;


    @Override
    public Optional<Product> getProductByProductId(ProductId productId) {
        return productJpaRepository.findById(productId.getValue())
                                   .map(productCommandPersistenceMapper::toProductDomainEntity);
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = productCommandPersistenceMapper.toProductEntity(product);
        return productCommandPersistenceMapper.toProductDomainEntity(productJpaRepository.save(productEntity));
    }

    @Override
    public Product update(Product product) {
        ProductEntity existingEntity = productJpaRepository.findById(product.getId().getValue())
                                                           .orElseThrow(() -> new IllegalStateException(
                                                                   "Product with id " + product.getId().getValue() +
                                                                   " not found!"));
        existingEntity.setName(product.getName());
        existingEntity.setDescription(product.getDescription());
        existingEntity.setPrice(product.getPrice().getAmount());
        existingEntity.setCategory(product.getCategory());

        return productCommandPersistenceMapper.toProductDomainEntity(productJpaRepository.save(existingEntity));
    }

    @Override
    public void deleteProductById(ProductId productId) {
        ProductEntity existingEntity = productJpaRepository.findById(productId.getValue())
                                                           .orElseThrow(() -> new IllegalStateException(
                                                                   "Product with id " + productId.getValue() +
                                                                   " not found!"));
        existingEntity.setStatus(ProductStatus.INACTIVE);
        productJpaRepository.save(existingEntity);
    }
}
