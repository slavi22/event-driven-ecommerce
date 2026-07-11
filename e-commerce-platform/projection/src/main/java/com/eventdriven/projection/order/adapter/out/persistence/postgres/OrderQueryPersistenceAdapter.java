package com.eventdriven.projection.order.adapter.out.persistence.postgres;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.port.out.query.GetAllOrdersQueryPort;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.port.out.query.SaveOrderQueryPort;
import com.eventdriven.projection.order.application.port.out.query.UpdateOrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrderQueryPersistenceAdapter implements GetOrderQueryPort, GetAllOrdersQueryPort,
        SaveOrderQueryPort, UpdateOrderQueryPort {

    private final OrderReadJpaRepository orderReadJpaRepository;
    private final OrderQueryPersistenceMapper orderQueryPersistenceMapper;

    @Override
    public Optional<GetOrderResult> getOrderById(UUID orderId) {
        return orderReadJpaRepository.findById(orderId)
                .map(orderQueryPersistenceMapper::toGetOrderResult);
    }

    @Override
    public List<GetOrderResult> getAllOrders() {
        return orderReadJpaRepository.findAll().stream()
                .map(orderQueryPersistenceMapper::toGetOrderResult)
                .toList();
    }

    @Override
    public GetOrderResult save(GetOrderResult result) {
        OrderReadEntity entity = orderQueryPersistenceMapper.toOrderReadEntity(result);
        entity.getItems().forEach(item -> item.setOrder(entity));
        return orderQueryPersistenceMapper.toGetOrderResult(orderReadJpaRepository.save(entity));
    }

    @Override
    public GetOrderResult update(GetOrderResult result) {
        OrderReadEntity entity = orderReadJpaRepository.findById(result.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order read entity not found for id " + result.orderId()));
        entity.setStatus(result.status());
        entity.setUpdatedAt(result.updatedAt());
        return orderQueryPersistenceMapper.toGetOrderResult(orderReadJpaRepository.save(entity));
    }
}
