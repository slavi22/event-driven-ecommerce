package com.eventdriven.order.domain.valueobject;

import com.eventdriven.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyValueObjectTest {

    @Test
    @DisplayName("Creating Money with valid non-negative amount should succeed")
    void testOf_withValidAmount_shouldCreateMoney() {
        // Arrange
        BigDecimal amount = new BigDecimal("9.99");

        // Act
        Money money = Money.of(amount);

        // Assert
        assertEquals(amount, money.getAmount());
    }

    @Test
    @DisplayName("Creating Money with zero amount should succeed")
    void testOf_withZeroAmount_shouldCreateMoney() {
        // Arrange
        BigDecimal amount = BigDecimal.ZERO;

        // Act
        Money money = Money.of(amount);

        // Assert
        assertEquals(amount, money.getAmount());
    }

    @Test
    @DisplayName("Creating Money with null amount should throw OrderDomainException")
    void testOf_withNullAmount_shouldThrowException() {
        // Act & Assert
        assertThrows(OrderDomainException.class, () -> Money.of(null));
    }

    @Test
    @DisplayName("Creating Money with negative amount should throw OrderDomainException")
    void testOf_withNegativeAmount_shouldThrowException() {
        // Arrange
        BigDecimal amount = new BigDecimal("-1.00");

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> Money.of(amount));
    }

    @Test
    @DisplayName("Adding two Money values should return their sum")
    void testAdd_shouldReturnSum() {
        // Arrange
        Money first = Money.of(new BigDecimal("5.00"));
        Money second = Money.of(new BigDecimal("3.00"));

        // Act
        Money result = first.add(second);

        // Assert
        assertEquals(Money.of(new BigDecimal("8.00")), result);
    }

    @Test
    @DisplayName("Multiplying Money by a positive factor should return correct result")
    void testMultiply_withPositiveFactor_shouldReturnCorrectResult() {
        // Arrange
        Money money = Money.of(new BigDecimal("10.00"));

        // Act
        Money result = money.multiply(3);

        // Assert
        assertEquals(Money.of(new BigDecimal("30.00")), result);
    }

    @Test
    @DisplayName("Multiplying Money by zero should return zero")
    void testMultiply_withZeroFactor_shouldReturnZero() {
        // Arrange
        Money money = Money.of(new BigDecimal("10.00"));

        // Act
        Money result = money.multiply(0);

        // Assert
        assertEquals(Money.of(BigDecimal.ZERO), result);
    }

    @Test
    @DisplayName("Multiplying Money by a negative factor should throw OrderDomainException")
    void testMultiply_withNegativeFactor_shouldThrowException() {
        // Arrange
        Money money = Money.of(new BigDecimal("10.00"));

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> money.multiply(-1));
    }

    @Test
    @DisplayName("Two Money instances with the same amount should be equal")
    void testEquals_withSameAmount_shouldBeEqual() {
        // Arrange
        Money first = Money.of(new BigDecimal("5.00"));
        Money second = Money.of(new BigDecimal("5.00"));

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
