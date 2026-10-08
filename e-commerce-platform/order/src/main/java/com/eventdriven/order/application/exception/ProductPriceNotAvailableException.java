package com.eventdriven.order.application.exception;

import application.exception.ApplicationException;

public class ProductPriceNotAvailableException extends ApplicationException {

    public ProductPriceNotAvailableException(String message) {
        super(message);
    }
}
