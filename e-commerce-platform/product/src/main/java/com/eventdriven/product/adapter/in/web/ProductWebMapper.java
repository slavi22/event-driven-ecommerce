package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
import com.eventdriven.product.adapter.in.web.dto.response.UpdateProductResponse;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.command.UpdateProductCommand;
import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.dto.UpdateProductResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductWebMapper {

    CreateProductCommand toCreateProductCommand(CreateProductRequest createProductRequest);

    @Mapping(target = "createdAt", expression = "java(ZonedDateTime.ofInstant(createProductResult.createdAt(), java.time.ZoneId.systemDefault()))")
    CreateProductResponse toCreateProductResponse(CreateProductResult createProductResult);

    UpdateProductCommand toUpdateProductCommand(UpdateProductRequest updateProductRequest);

    UpdateProductResponse toUpdateProductResponse(UpdateProductResult updateProductResult);

    /*default ZonedDateTime map(Instant value, CreateProductResult createProductResult) {

        createProductResult.createdAt()
        return ZonedDateTime.ofInstant(value, ZoneId.systemDefault());


    }*/
}
