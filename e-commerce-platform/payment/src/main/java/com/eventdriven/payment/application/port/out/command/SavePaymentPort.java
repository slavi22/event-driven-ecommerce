package com.eventdriven.payment.application.port.out.command;

import com.eventdriven.payment.domain.entity.Payment;

public interface SavePaymentPort {
    void save(Payment payment);
}
