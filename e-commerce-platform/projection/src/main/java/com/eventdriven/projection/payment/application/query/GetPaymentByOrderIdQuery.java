package com.eventdriven.projection.payment.application.query;

import java.util.UUID;

public record GetPaymentByOrderIdQuery(UUID orderId) {
}
