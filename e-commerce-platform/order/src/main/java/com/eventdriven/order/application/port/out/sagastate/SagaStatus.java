package com.eventdriven.order.application.port.out.sagastate;

public enum SagaStatus {
    STARTED,
    COMPLETED,
    COMPENSATING,
    FAILED
}
