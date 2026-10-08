package com.eventdriven.projection.order.adapter.out.persistence.postgres;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.dto.OrderItemResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface OrderQueryPersistenceMapper {

    @Mapping(target = "orderId", source = "id")
    GetOrderResult toGetOrderResult(OrderReadEntity entity);

    @Mapping(target = "id", source = "orderId")
    OrderReadEntity toOrderReadEntity(GetOrderResult result);

    OrderItemResult toOrderItemResult(OrderItemReadEntity entity);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "order", ignore = true)
    OrderItemReadEntity toOrderItemReadEntity(OrderItemResult result);
}
