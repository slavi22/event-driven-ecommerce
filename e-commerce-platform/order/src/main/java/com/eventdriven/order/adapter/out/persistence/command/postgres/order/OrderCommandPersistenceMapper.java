package com.eventdriven.order.adapter.out.persistence.command.postgres.order;

import com.eventdriven.order.domain.entity.Order;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderId;
import com.eventdriven.order.domain.valueobject.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface OrderCommandPersistenceMapper {

    default OrderEntity toOrderEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().getValue());
        entity.setCustomerId(order.getCustomerId());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toOrderItemEntity(item, entity))
                .toList();
        entity.setItems(itemEntities);
        return entity;
    }

    default OrderItemEntity toOrderItemEntity(OrderItem item, OrderEntity parent) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setOrder(parent);
        entity.setProductId(item.getProductId());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice().getAmount());
        return entity;
    }

    default Order toOrder(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> OrderItem.of(
                        i.getProductId(),
                        i.getQuantity(),
                        Money.of(i.getUnitPrice())))
                .toList();
        return Order.reconstitute(
                new OrderId(entity.getId()),
                entity.getCustomerId(),
                items,
                Money.of(entity.getTotalAmount()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
