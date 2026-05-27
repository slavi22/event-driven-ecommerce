package com.eventdriven.product.adapter.out.persistence.command.postgres;

import com.eventdriven.product.application.port.out.persistence.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistence.SaveProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductCommandPersistenceAdapter implements SaveProductPort, DeleteProductPort {

    private final ProductJpaRepository productJpaRepository;
    private final PersistenceMapper persistenceMapper;


    @Override
    public Product save(Product product) {
        ProductEntity productEntity = persistenceMapper.toProductEntity(product);
        return persistenceMapper.toProductDomainEntity(productJpaRepository.save(productEntity));
    }

    @Override
    public void deleteProductById(ProductId productId) {
        // TODO: decide if we want to implement soft delete or hard delete, and implement accordingly
        throw new UnsupportedOperationException("Delete product is not implemented yet!");
    }

}
