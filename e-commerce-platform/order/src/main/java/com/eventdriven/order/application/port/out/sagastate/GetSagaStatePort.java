package com.eventdriven.order.application.port.out.sagastate;

import java.util.UUID;

public interface GetSagaStatePort {

    SagaState getSagaState(UUID orderId);
}
