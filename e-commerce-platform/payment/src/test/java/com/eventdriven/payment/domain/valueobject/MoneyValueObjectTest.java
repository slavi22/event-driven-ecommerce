package com.eventdriven.payment.domain.valueobject;

import com.eventdriven.payment.domain.exception.PaymentDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyValueObjectTest {

    @Test
    @DisplayName("Creating Money with a valid non-negative amount should succeed")
    void testOf_withValidAmount_shouldCreateMoney() {
        BigDecimal amount = new BigDecimal("9.99");

        Money money = Money.of(amount);

        assertEquals(amount, money.getAmount());
    }

    @Test
    @DisplayName("Creating Money with zero should succeed")
    void testOf_withZeroAmount_shouldCreateMoney() {
        Money money = Money.of(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, money.getAmount());
    }

    @Test
    @DisplayName("Creating Money with null should throw PaymentDomainException")
    void testOf_withNullAmount_shouldThrowException() {
        assertThrows(PaymentDomainException.class, () -> Money.of(null));
    }

    @Test
    @DisplayName("Creating Money with a negative amount should throw PaymentDomainException")
    void testOf_withNegativeAmount_shouldThrowException() {
        assertThrows(PaymentDomainException.class, () -> Money.of(new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("Adding two Money values should return their sum")
    void testAdd_shouldReturnSum() {
        Money first = Money.of(new BigDecimal("5.00"));
        Money second = Money.of(new BigDecimal("3.00"));

        assertEquals(Money.of(new BigDecimal("8.00")), first.add(second));
    }

    @Test
    @DisplayName("Multiplying Money by a positive factor should return the correct result")
    void testMultiply_withPositiveFactor_shouldReturnCorrectResult() {
        Money money = Money.of(new BigDecimal("10.00"));

        assertEquals(Money.of(new BigDecimal("30.00")), money.multiply(3));
    }

    @Test
    @DisplayName("Multiplying Money by zero should return zero")
    void testMultiply_withZeroFactor_shouldReturnZero() {
        Money money = Money.of(new BigDecimal("10.00"));

        assertEquals(Money.of(BigDecimal.ZERO), money.multiply(0));
    }

    @Test
    @DisplayName("Multiplying Money by a negative factor should throw PaymentDomainException")
    void testMultiply_withNegativeFactor_shouldThrowException() {
        Money money = Money.of(new BigDecimal("10.00"));

        assertThrows(PaymentDomainException.class, () -> money.multiply(-1));
    }

    @Test
    @DisplayName("Two Money instances with the same amount should be equal")
    void testEquals_withSameAmount_shouldBeEqual() {
        Money first = Money.of(new BigDecimal("5.00"));
        Money second = Money.of(new BigDecimal("5.00"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
