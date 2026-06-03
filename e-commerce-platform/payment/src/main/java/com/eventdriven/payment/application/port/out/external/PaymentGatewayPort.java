package com.eventdriven.payment.application.port.out.external;

import com.eventdriven.payment.domain.entity.Payment;

public interface PaymentGatewayPort {
    PaymentGatewayResult charge(Payment payment);
}
