package com.eventdriven.order.application.port.out.sagastate;

import java.time.Instant;
import java.util.UUID;

public record SagaState(UUID orderId,
                        SagaStep currentStep,
                        SagaStatus status,
                        Instant createdAt,
                        Instant updatedAt) {
}
