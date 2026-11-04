package com.eventdriven.payment.application.port.in.command;

import com.eventdriven.payment.application.command.ProcessPaymentCommand;

public interface ProcessPaymentUseCase {
    void processPayment(ProcessPaymentCommand command);
}
