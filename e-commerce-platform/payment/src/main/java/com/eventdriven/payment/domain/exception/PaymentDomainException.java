package com.eventdriven.payment.domain.exception;

import domain.exception.DomainException;

public class PaymentDomainException extends DomainException {

    public PaymentDomainException(String message) {
        super(message);
    }
}
