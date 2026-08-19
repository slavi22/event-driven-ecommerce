package com.eventdriven.stock.domain.valueobject;

import com.eventdriven.stock.domain.exception.StockDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuantityValueObjectTest {

    @Test
    @DisplayName("Creating a quantity with a positive value should succeed")
    void testOf_withPositiveValue_shouldCreateQuantity() {
        // Act
        Quantity quantity = Quantity.of(10);

        // Assert
        assertEquals(10, quantity.getValue());
    }

    @Test
    @DisplayName("Creating a quantity of zero should succeed")
    void testOf_withZeroValue_shouldCreateQuantity() {
        // Act
        Quantity quantity = Quantity.of(0);

        // Assert
        assertEquals(0, quantity.getValue());
    }

    @Test
    @DisplayName("Creating a quantity with a negative value should throw StockDomainException")
    void testOf_withNegativeValue_shouldThrowStockDomainException() {
        // Act & Assert
        assertThrows(StockDomainException.class, () -> Quantity.of(-1));
    }

    @Test
    @DisplayName("Adding two quantities should return a new quantity with the sum")
    void testAdd_shouldReturnSumAsNewQuantity() {
        // Arrange
        Quantity a = Quantity.of(10);
        Quantity b = Quantity.of(5);

        // Act
        Quantity result = a.add(b);

        // Assert
        assertEquals(15, result.getValue());
    }

    @Test
    @DisplayName("Subtracting a smaller quantity should return the difference")
    void testSubtract_withSufficientValue_shouldReturnDifference() {
        // Arrange
        Quantity a = Quantity.of(10);
        Quantity b = Quantity.of(4);

        // Act
        Quantity result = a.subtract(b);

        // Assert
        assertEquals(6, result.getValue());
    }

    @Test
    @DisplayName("Subtracting equal quantities should return zero")
    void testSubtract_withEqualValue_shouldReturnZero() {
        // Arrange
        Quantity a = Quantity.of(7);

        // Act
        Quantity result = a.subtract(Quantity.of(7));

        // Assert
        assertEquals(0, result.getValue());
    }

    @Test
    @DisplayName("Subtracting a larger quantity should throw StockDomainException")
    void testSubtract_withValueExceedingCurrent_shouldThrowStockDomainException() {
        // Arrange
        Quantity a = Quantity.of(3);

        // Act & Assert
        assertThrows(StockDomainException.class, () -> a.subtract(Quantity.of(5)));
    }

    @Test
    @DisplayName("Two quantities with the same value should be equal")
    void testEquals_withSameValue_shouldBeEqual() {
        // Act & Assert
        assertEquals(Quantity.of(10), Quantity.of(10));
    }

    @Test
    @DisplayName("Two quantities with different values should not be equal")
    void testEquals_withDifferentValues_shouldNotBeEqual() {
        // Act & Assert
        assertNotEquals(Quantity.of(10), Quantity.of(20));
    }
}
