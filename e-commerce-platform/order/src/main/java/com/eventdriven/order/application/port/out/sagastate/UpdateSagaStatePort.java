package com.eventdriven.order.application.port.out.sagastate;

import java.util.UUID;

public interface UpdateSagaStatePort {

    void update(UUID orderId, SagaStep step, SagaStatus status);
}
