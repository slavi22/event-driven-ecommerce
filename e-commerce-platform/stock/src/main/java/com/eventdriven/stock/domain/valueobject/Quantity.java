package com.eventdriven.stock.domain.valueobject;

import com.eventdriven.stock.domain.exception.StockDomainException;

import java.util.Objects;

public class Quantity {
    private final int value;

    public static Quantity of(int value) {
        if (value < 0) {
            throw new StockDomainException("Quantity cannot be negative");
        }
        return new Quantity(value);
    }

    public Quantity add(Quantity amount) {
        return of(this.value + amount.getValue());
    }

    public Quantity subtract(Quantity amount) {
        return of(this.value - amount.getValue());
    }

    private Quantity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
