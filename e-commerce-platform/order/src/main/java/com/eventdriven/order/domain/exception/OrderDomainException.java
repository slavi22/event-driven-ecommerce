package com.eventdriven.order.domain.exception;

import domain.exception.DomainException;

public class OrderDomainException extends DomainException {

    public OrderDomainException(String message) {
        super(message);
    }
}
