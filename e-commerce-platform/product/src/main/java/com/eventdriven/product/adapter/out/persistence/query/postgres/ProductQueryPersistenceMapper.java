package com.eventdriven.product.adapter.out.persistence.query.postgres;

import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductQueryPersistenceMapper {

    default Product toProductDomainEntity(ProductReadEntity entity) {
        return Product.reconstitute(
                new ProductId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                Money.of(entity.getPrice()),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Mapping(target = "id", expression = "java(product.getId().getValue())")
    @Mapping(target = "price", expression = "java(product.getPrice().getAmount())")
    ProductReadEntity toProductReadEntity(Product product);
}
