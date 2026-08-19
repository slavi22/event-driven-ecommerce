package com.eventdriven.product.domain.exception;

import domain.exception.DomainException;

public class ProductDomainException extends DomainException {
    public ProductDomainException(String message) {
        super(message);
    }
}
