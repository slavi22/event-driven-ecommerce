package com.eventdriven.projection.product.adapter.out.persistence.postgres;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductQueryPersistenceMapper {

    @Mapping(target = "productId", expression = "java(entity.getId().toString())")
    GetProductResult toGetProductResult(ProductReadEntity entity);

    @Mapping(target = "id", expression = "java(java.util.UUID.fromString(result.productId()))")
    ProductReadEntity toProductReadEntity(GetProductResult result);
}
