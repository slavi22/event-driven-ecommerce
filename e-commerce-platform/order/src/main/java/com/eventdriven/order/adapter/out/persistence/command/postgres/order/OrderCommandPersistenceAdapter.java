package com.eventdriven.order.adapter.out.persistence.command.postgres.order;

import com.eventdriven.order.application.exception.OrderNotFoundException;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.command.SaveOrderPort;
import com.eventdriven.order.application.port.out.command.UpdateOrderPort;
import com.eventdriven.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrderCommandPersistenceAdapter implements SaveOrderPort, UpdateOrderPort, GetOrderPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderCommandPersistenceMapper orderCommandPersistenceMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderCommandPersistenceMapper.toOrderEntity(order);
        return orderCommandPersistenceMapper.toOrder(orderJpaRepository.save(entity));
    }

    @Override
    public void update(Order order) {
        OrderEntity entity = orderJpaRepository.findById(order.getId().getValue())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + order.getId().getValue() + " not found"));
        entity.setStatus(order.getStatus());
        entity.setUpdatedAt(order.getUpdatedAt());
        orderJpaRepository.save(entity);
    }

    @Override
    public Order getOrder(UUID orderId) {
        return orderJpaRepository.findByIdWithItems(orderId)
                .map(orderCommandPersistenceMapper::toOrder)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + orderId + " not found"));
    }
}
