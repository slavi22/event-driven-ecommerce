package com.eventdriven.order.adapter.out.persistence.command.postgres.sagastate;

import com.eventdriven.order.application.exception.OrderNotFoundException;
import com.eventdriven.order.application.port.out.sagastate.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class SagaStatePersistenceAdapter implements SaveSagaStatePort, UpdateSagaStatePort, GetSagaStatePort {

    private final SagaStateJpaRepository sagaStateJpaRepository;

    @Override
    public void save(SagaState state) {
        SagaStateEntity entity = new SagaStateEntity();
        entity.setOrderId(state.orderId());
        entity.setCurrentStep(state.currentStep());
        entity.setStatus(state.status());
        entity.setCreatedAt(state.createdAt());
        entity.setUpdatedAt(state.updatedAt());
        sagaStateJpaRepository.save(entity);
    }

    @Override
    public void update(UUID orderId, SagaStep step, SagaStatus status) {
        SagaStateEntity entity = sagaStateJpaRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Saga state for order " + orderId + " not found"));
        entity.setCurrentStep(step);
        entity.setStatus(status);
        entity.setUpdatedAt(Instant.now());
        sagaStateJpaRepository.save(entity);
    }

    @Override
    public SagaState getSagaState(UUID orderId) {
        return sagaStateJpaRepository.findById(orderId)
                .map(e -> new SagaState(e.getOrderId(), e.getCurrentStep(), e.getStatus(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .orElseThrow(() -> new OrderNotFoundException(
                        "Saga state for order " + orderId + " not found"));
    }
}
