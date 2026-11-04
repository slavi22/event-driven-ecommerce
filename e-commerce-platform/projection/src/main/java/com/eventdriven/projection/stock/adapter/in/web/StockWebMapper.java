package com.eventdriven.projection.stock.adapter.in.web;

import com.eventdriven.projection.stock.adapter.in.web.dto.response.GetStockResponse;
import com.eventdriven.projection.stock.application.dto.GetStockResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockWebMapper {
    GetStockResponse toGetStockResponse(GetStockResult result);
}
