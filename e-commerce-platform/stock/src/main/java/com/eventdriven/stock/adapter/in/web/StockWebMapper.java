package com.eventdriven.stock.adapter.in.web;

import com.eventdriven.stock.adapter.in.web.dto.request.ReplenishStockRequest;
import com.eventdriven.stock.adapter.in.web.dto.response.ReplenishStockResponse;
import com.eventdriven.stock.application.command.ReplenishStockCommand;
import com.eventdriven.stock.application.dto.ReplenishStockResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockWebMapper {

    @Mapping(target = "productId", expression = "java(java.util.UUID.fromString(productId))")
    @Mapping(source = "request.amount", target = "amount")
    ReplenishStockCommand toReplenishStockCommand(String productId, ReplenishStockRequest request);

    ReplenishStockResponse toReplenishStockResponse(ReplenishStockResult result);
}
