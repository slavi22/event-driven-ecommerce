package com.eventdriven.stock.domain.exception;

import domain.exception.DomainException;

public class StockDomainException extends DomainException {

    public StockDomainException(String message) {
        super(message);
    }
}
