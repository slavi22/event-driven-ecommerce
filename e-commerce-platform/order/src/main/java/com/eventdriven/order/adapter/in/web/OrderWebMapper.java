package com.eventdriven.order.adapter.in.web;

import com.eventdriven.order.adapter.in.web.dto.request.OrderItemRequest;
import com.eventdriven.order.adapter.in.web.dto.request.PlaceOrderRequest;
import com.eventdriven.order.adapter.in.web.dto.response.PlaceOrderResponse;
import com.eventdriven.order.application.command.OrderItemCommand;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderWebMapper {

    @Mapping(target = "customerId", expression = "java(java.util.UUID.fromString(customerId))")
    @Mapping(source = "request.items", target = "items")
    PlaceOrderCommand toPlaceOrderCommand(String customerId, PlaceOrderRequest request);

    OrderItemCommand toOrderItemCommand(OrderItemRequest request);

    PlaceOrderResponse toPlaceOrderResponse(PlaceOrderResult result);
}
