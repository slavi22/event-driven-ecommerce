package com.eventdriven.order.application.port.out.sagastate;

public interface SaveSagaStatePort {

    void save(SagaState state);
}
