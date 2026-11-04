package com.eventdriven.payment.application.port.out.command;

import com.eventdriven.payment.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface GetPaymentPort {
    Optional<Payment> findByOrderId(UUID orderId);
}
