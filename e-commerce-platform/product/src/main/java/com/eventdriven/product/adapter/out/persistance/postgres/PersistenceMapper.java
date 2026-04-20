package com.eventdriven.product.adapter.out.persistance.postgres;

import com.eventdriven.product.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersistenceMapper {
    Product toProductDomainEntity(ProductEntity productEntity);

    ProductEntity toProductEntity(Product productDomainEntity);
}
