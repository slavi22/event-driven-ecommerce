package com.eventdriven.order.application.port.out.sagastate;

public enum SagaStep {
    STOCK_RESERVATION,
    PAYMENT,
    COMPENSATING_STOCK,
    COMPLETED
}
