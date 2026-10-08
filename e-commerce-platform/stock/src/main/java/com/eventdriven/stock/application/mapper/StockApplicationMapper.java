package com.eventdriven.stock.application.mapper;

import com.eventdriven.stock.application.dto.ReplenishStockResult;
import com.eventdriven.stock.domain.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockApplicationMapper {

    @Mapping(target = "newQuantity", expression = "java(stock.getQuantity().getValue())")
    ReplenishStockResult toReplenishStockResult(Stock stock);
}
