package com.eventdriven.payment.application.port.out.external;

public record PaymentGatewayResult(boolean success, String reason) {
}
