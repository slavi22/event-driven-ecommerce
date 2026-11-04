package com.eventdriven.order.application.exception;

import application.exception.ApplicationException;

public class OrderNotFoundException extends ApplicationException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
