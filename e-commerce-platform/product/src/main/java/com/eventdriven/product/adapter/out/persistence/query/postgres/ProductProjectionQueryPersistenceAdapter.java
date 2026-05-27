package com.eventdriven.product.adapter.out.persistence.query.postgres;

import com.eventdriven.product.application.port.out.persistence.GetProductProjectionPort;
import com.eventdriven.product.application.port.out.persistence.GetProductsProjectionPort;
import com.eventdriven.product.application.port.out.persistence.SaveProductProjectionPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ProductProjectionQueryPersistenceAdapter implements GetProductProjectionPort, GetProductsProjectionPort, SaveProductProjectionPort {

    private final ProductReadJpaRepository productReadJpaRepository;
    private final ProductReadMapper productReadMapper;

    @Override
    public List<Product> getProducts() {
        return productReadJpaRepository.findAll()
                                       .stream()
                                       .map(productReadMapper::toProductDomainEntity)
                                       .toList();
    }

    @Override
    public Optional<Product> getProductByProductId(ProductId productId) {
        return productReadJpaRepository.findById(productId.getValue())
                                       .map(productReadMapper::toProductDomainEntity);
    }

    @Override
    public Product save(Product product) {
        ProductReadEntity entity = productReadMapper.toProductReadEntity(product);
        return productReadMapper.toProductDomainEntity(productReadJpaRepository.save(entity));
    }
}
