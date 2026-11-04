package com.eventdriven.product.application.exception;

import application.exception.ApplicationException;

public class ProductNotFoundException extends ApplicationException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
