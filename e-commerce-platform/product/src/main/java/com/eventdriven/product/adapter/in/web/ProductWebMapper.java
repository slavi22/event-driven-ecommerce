package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductWebMapper {

    CreateProductCommand toCreateProductCommand(CreateProductRequest createProductRequest);
    CreateProductResponse toCreateProductResponse(ProductResponse productResponse);
}
