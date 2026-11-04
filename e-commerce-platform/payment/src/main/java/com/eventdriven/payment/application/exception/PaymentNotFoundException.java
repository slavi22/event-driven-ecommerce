package com.eventdriven.payment.application.exception;

import application.exception.ApplicationException;

public class PaymentNotFoundException extends ApplicationException {

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
