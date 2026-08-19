package com.eventdriven.product.application.mapper;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.dto.UpdateProductResult;
import com.eventdriven.product.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductApplicationMapper {
    @Mapping(target = "price", expression = "java(productDomainEntity.getPrice().getAmount())")
    CreateProductResult toCreateProductResult(Product productDomainEntity);

    List<CreateProductResult> toCreateProductResultList(List<Product> products);

    @Mapping(target = "price", expression = "java(productDomainEntity.getPrice().getAmount())")
    UpdateProductResult toUpdateProductResult(Product productDomainEntity);
}
