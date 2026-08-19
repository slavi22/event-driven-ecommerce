package com.eventdriven.projection.stock.application.exception;

import application.exception.ApplicationException;

public class StockNotFoundException extends ApplicationException {
    public StockNotFoundException(String message) {
        super(message);
    }
}
