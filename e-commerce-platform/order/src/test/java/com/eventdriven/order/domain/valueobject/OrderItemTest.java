package com.eventdriven.order.domain.valueobject;

import com.eventdriven.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("Creating OrderItem with valid fields should succeed")
    void testOf_withValidFields_shouldCreateOrderItem() {
        // Arrange
        UUID productId = UUID.randomUUID();
        int quantity = 3;
        Money unitPrice = Money.of(new BigDecimal("5.00"));

        // Act
        OrderItem item = OrderItem.of(productId, quantity, unitPrice);

        // Assert
        assertEquals(productId, item.getProductId());
        assertEquals(quantity, item.getQuantity());
        assertEquals(unitPrice, item.getUnitPrice());
    }

    @Test
    @DisplayName("lineTotal should return unitPrice multiplied by quantity")
    void testLineTotal_shouldReturnCorrectValue() {
        // Arrange
        Money unitPrice = Money.of(new BigDecimal("5.00"));
        OrderItem item = OrderItem.of(UUID.randomUUID(), 4, unitPrice);

        // Act
        Money lineTotal = item.lineTotal();

        // Assert
        assertEquals(Money.of(new BigDecimal("20.00")), lineTotal);
    }

    @Test
    @DisplayName("Creating OrderItem with null productId should throw OrderDomainException")
    void testOf_withNullProductId_shouldThrowException() {
        // Arrange
        int quantity = 1;
        Money unitPrice = Money.of(BigDecimal.ONE);

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> OrderItem.of(null, quantity, unitPrice));
    }

    @Test
    @DisplayName("Creating OrderItem with zero quantity should throw OrderDomainException")
    void testOf_withZeroQuantity_shouldThrowException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Money unitPrice = Money.of(BigDecimal.ONE);

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> OrderItem.of(productId, 0, unitPrice));
    }

    @Test
    @DisplayName("Creating OrderItem with negative quantity should throw OrderDomainException")
    void testOf_withNegativeQuantity_shouldThrowException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Money unitPrice = Money.of(BigDecimal.ONE);

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> OrderItem.of(productId, -1, unitPrice));
    }

    @Test
    @DisplayName("Creating OrderItem with null unit price should throw OrderDomainException")
    void testOf_withNullUnitPrice_shouldThrowException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        int quantity = 1;

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> OrderItem.of(productId, quantity, null));
    }

    @Test
    @DisplayName("Two OrderItem instances with the same values should be equal")
    void testEquals_withSameValues_shouldBeEqual() {
        // Arrange
        UUID productId = UUID.randomUUID();

        OrderItem first = OrderItem.of(
                productId,
                2,
                Money.of(new BigDecimal("5.00")));

        OrderItem second = OrderItem.of(
                productId,
                2,
                Money.of(new BigDecimal("5.00")));

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
