package com.eventdriven.order.application.mapper;

import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.domain.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderApplicationMapper {

    @Mapping(target = "orderId", expression = "java(order.getId().getValue())")
    PlaceOrderResult toPlaceOrderResult(Order order);
}
