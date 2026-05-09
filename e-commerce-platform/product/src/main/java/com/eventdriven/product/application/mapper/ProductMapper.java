package com.eventdriven.product.application.mapper;

import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    ProductResponse toProductResponse(Product productDomainEntity);
}
