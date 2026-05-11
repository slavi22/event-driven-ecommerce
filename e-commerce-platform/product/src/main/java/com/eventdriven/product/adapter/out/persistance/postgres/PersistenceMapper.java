package com.eventdriven.product.adapter.out.persistance.postgres;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersistenceMapper {
    default Product toProductDomainEntity(ProductEntity productEntity) {
        return Product.reconstitute(
                new ProductId(productEntity.getId()),
                productEntity.getName(),
                productEntity.getDescription(),
                Money.of(productEntity.getPrice()),
                productEntity.getCategory(),
                productEntity.getStatus(),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt()
        );
    }

    @Mapping(target = "id", expression = "java(product.getId().getValue())")
    @Mapping(target = "price", expression = "java(product.getPrice().getAmount())")
    ProductEntity toProductEntity(Product productDomainEntity);
}
