package com.eventdriven.order.domain.valueobject;

import com.eventdriven.order.domain.exception.OrderDomainException;

import java.math.BigDecimal;

public class Money {

    private final BigDecimal amount;

    public static Money of(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderDomainException("Amount must be non-negative");
        }
        return new Money(amount);
    }

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money add(Money other) {
        return of(this.amount.add(other.amount));
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new OrderDomainException("Multiplication factor cannot be negative");
        }
        return of(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }
}
