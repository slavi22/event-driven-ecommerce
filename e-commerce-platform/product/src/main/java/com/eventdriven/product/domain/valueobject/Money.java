package com.eventdriven.product.domain.valueobject;

import com.eventdriven.product.domain.exception.ProductDomainException;

import java.math.BigDecimal;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;

    public static Money of(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductDomainException("Amount must be non-negative");
        }
        return new Money(amount);
    }

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money add(Money other) {
        return of(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (this.amount.compareTo(other.amount) < 0) {
            throw new ProductDomainException("Resulting amount cannot be negative");
        }
        return of(this.amount.subtract(other.amount));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }
}
