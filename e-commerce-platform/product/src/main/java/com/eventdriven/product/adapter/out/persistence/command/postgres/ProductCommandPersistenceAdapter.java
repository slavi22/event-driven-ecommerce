package com.eventdriven.product.adapter.out.persistence.command.postgres;

import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.port.out.persistence.command.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.command.SaveProductPort;
import com.eventdriven.product.application.port.out.persistence.command.UpdateProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
class ProductCommandPersistenceAdapter implements GetProductCommandPort, SaveProductPort, UpdateProductPort, DeleteProductPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductCommandPersistenceMapper productCommandPersistenceMapper;


    @Override
    public Product getProductByProductId(ProductId productId) {
        return productCommandPersistenceMapper.toProductDomainEntity(
                productJpaRepository.findById(productId.getValue()).orElseThrow(
                        () -> new ProductNotFoundException("Product with id " + productId.getValue() + " not found!")));
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = productCommandPersistenceMapper.toProductEntity(product);
        return productCommandPersistenceMapper.toProductDomainEntity(productJpaRepository.save(productEntity));
    }

    @Override
    public Product update(Product product) {
        ProductEntity existingEntity = productJpaRepository.findById(product.getId().getValue())
                                                           .orElseThrow(() -> new ProductNotFoundException(
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
        // TODO: decide if we want to implement soft delete or hard delete, and implement accordingly
        throw new UnsupportedOperationException("Delete product is not implemented yet!");
    }
}
