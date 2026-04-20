package com.eventdriven.product.adapter.out.persistance.postgres;

import com.eventdriven.product.application.port.out.persistance.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistance.GetProductPort;
import com.eventdriven.product.application.port.out.persistance.SaveProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements SaveProductPort, GetProductPort, DeleteProductPort {

    private final ProductJpaRepository productJpaRepository;
    private final PersistenceMapper persistenceMapper;


    @Override
    public Product saveProduct(Product product) {
        ProductEntity productEntity = persistenceMapper.toProductEntity(product);
        return persistenceMapper.toProductDomainEntity(productJpaRepository.save(productEntity));
    }

    @Override
    public List<Product> getProducts() {
        return productJpaRepository.findAll()
                                   .stream()
                                   .map(persistenceMapper::toProductDomainEntity)
                                   .toList();
    }

    @Override
    public Optional<Product> getProductByProductId(ProductId productId) {
        Optional<ProductEntity> productEntity = productJpaRepository.findById(productId.getValue());
        return productEntity.map(persistenceMapper::toProductDomainEntity);
    }

    @Override
    public void deleteProductById(ProductId productId) {
        // TODO: decide if we want to implement soft delete or hard delete, and implement accordingly
        throw new UnsupportedOperationException("Delete product is not implemented yet!");
    }

}
