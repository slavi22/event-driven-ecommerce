package com.eventdriven.product.application.mapper;

import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    @Mapping(target = "price", source = "price")
    ProductResponse toProductResponse(Product productDomainEntity);

    default BigDecimal map(Money value) {
        return value.getAmount();
    }
}
