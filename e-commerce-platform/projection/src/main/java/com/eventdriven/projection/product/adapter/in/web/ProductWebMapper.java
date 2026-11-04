package com.eventdriven.projection.product.adapter.in.web;

import com.eventdriven.projection.product.adapter.in.web.dto.response.GetProductResponse;
import com.eventdriven.projection.product.application.dto.GetProductResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductWebMapper {

    GetProductResponse toGetProductResponse(GetProductResult result);

    List<GetProductResponse> toGetProductResponseList(List<GetProductResult> results);
}
