package com.eventdriven.product.domain.valueobject;

import com.eventdriven.product.domain.exception.ProductDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyValueObjectTest {

    @Test
    @DisplayName("Creating a Money value object with a valid amount should create the Money value object successfully")
    void testCreateMoneyValueObject_whenAmountIsValid_shouldCreateMoneyValueObject() {
        // Arrange
        BigDecimal amount = new BigDecimal("9.99");
        Money money = Money.of(amount);

        // Act & Assert
        assertEquals(amount, money.getAmount());
    }

    @Test
    @DisplayName("Creating a Money value object with a negative amount should throw ProductDomainException")
    void testCreateMoneyValueObject_whenAmountIsNull_shouldThrowProductDomainException() {
        // Arrange
        BigDecimal amount = null;

        // Act & Assert
        assertThrows(ProductDomainException.class, () -> Money.of(amount));
    }

    @Test
    @DisplayName("Creating a Money value object with a negative amount should throw ProductDomainException")
    void testCreateMoneyValueObject_whenAmountValueIsLessThanZero_shouldProductDomainException() {
        // Arrange
        BigDecimal amount = new BigDecimal("-1.00");

        // Act & Assert
        assertThrows(ProductDomainException.class, () -> Money.of(amount));
    }

    @Test
    @DisplayName("Adding two Money value objects with valid amounts should return a new Money value object with the correct amount")
    void testMoneyValueObjectAddOperation_withValidAmount_shouldReturnCorrectResult() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("10.00"));
        Money money2 = Money.of(new BigDecimal("5.00"));

        BigDecimal expectedResult = new BigDecimal("15.00");

        // Act
        Money result = money1.add(money2);

        // Assert
        assertEquals(expectedResult, result.getAmount());
    }

    @Test
    @DisplayName("Subtracting a Money value object from another Money value object with valid amounts should return a new Money value object with the correct amount")
    void testMoneyValueObjectSubtractOperation_withValidAmount_shouldReturnCorrectResult() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("10.00"));
        Money money2 = Money.of(new BigDecimal("5.00"));

        BigDecimal expectedResult = new BigDecimal("5.00");

        // Act
        Money result = money1.subtract(money2);

        // Assert
        assertEquals(expectedResult, result.getAmount());
    }

    @Test
    @DisplayName("Subtracting a Money value object from another Money value object with an amount that would result in a negative amount should throw ProductDomainException")
    void testMoneyValueObjectSubtractOperation_withInvalidAmount_shouldThrowProductDomainException() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("5.00"));
        Money money2 = Money.of(new BigDecimal("10.00"));

        // Act & Assert
        assertThrows(ProductDomainException.class, () -> money1.subtract(money2));
    }

    @Test
    @DisplayName("Comparing two Money value objects with positive amount using isGreaterThan should return the correct result")
    void testMoneyValueObjectIsGreaterThanOperation_withPositiveAmount_shouldReturnCorrectResult() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("10.00"));
        Money money2 = Money.of(new BigDecimal("5.00"));

        // Act
        boolean result = money1.isGreaterThan(money2);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Comparing two Money value objects with negative amount using isGreaterThan should return the correct result")
    void testMoneyValueObjectIsGreaterThanOperation_withNegativeAmount_shouldReturnCorrectResult() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("5.00"));
        Money money2 = Money.of(new BigDecimal("10.00"));

        // Act
        boolean result = money1.isGreaterThan(money2);

        // Assert
        assertFalse(result);
    }
}
