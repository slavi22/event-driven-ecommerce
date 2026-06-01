package com.eventdriven.stock.domain.entity;

import com.eventdriven.stock.domain.exception.StockDomainException;
import com.eventdriven.stock.domain.valueobject.Quantity;
import com.eventdriven.stock.domain.valueobject.StockId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StockDomainEntityTest {

    @Test
    @DisplayName("Initializing stock with valid params should create a stock entity")
    void testInitialize_withValidParams_shouldCreateStock() {
        // Arrange
        UUID productId = UUID.randomUUID();

        // Act
        Stock stock = Stock.initialize(productId, 100);

        // Assert
        assertNotNull(stock.getId());
        assertEquals(productId, stock.getProductId());
        assertEquals(100, stock.getQuantity().getValue());
        assertNotNull(stock.getCreatedAt());
        assertNotNull(stock.getUpdatedAt());
    }

    @Test
    @DisplayName("Initializing stock with null productId should throw StockDomainException")
    void testInitialize_withNullProductId_shouldThrowStockDomainException() {
        // Act & Assert
        assertThrows(StockDomainException.class, () -> Stock.initialize(null, 10));
    }

    @Test
    @DisplayName("Initializing stock with negative quantity should throw StockDomainException")
    void testInitialize_withNegativeQuantity_shouldThrowStockDomainException() {
        // Arrange
        UUID productId = UUID.randomUUID();

        // Act & Assert
        assertThrows(StockDomainException.class, () -> Stock.initialize(productId, -1));
    }

    @Test
    @DisplayName("Replenishing stock should increase the quantity by the given amount")
    void testReplenish_withPositiveAmount_shouldIncreaseQuantity() {
        // Arrange
        Stock stock = buildExistingStock(50);

        // Act
        stock.replenish(30);

        // Assert
        assertEquals(80, stock.getQuantity().getValue());
    }

    @Test
    @DisplayName("Reserving stock with sufficient quantity should decrease the quantity by the given amount")
    void testReserve_withSufficientStock_shouldDecreaseQuantity() {
        // Arrange
        Stock stock = buildExistingStock(100);

        // Act
        stock.reserve(40);

        // Assert
        assertEquals(60, stock.getQuantity().getValue());
    }

    @Test
    @DisplayName("Reserving stock with exact available quantity should set quantity to zero")
    void testReserve_withExactAvailableStock_shouldSetQuantityToZero() {
        // Arrange
        Stock stock = buildExistingStock(10);

        // Act
        stock.reserve(10);

        // Assert
        assertEquals(0, stock.getQuantity().getValue());
    }

    @Test
    @DisplayName("Reserving more stock than available should throw StockDomainException")
    void testReserve_withInsufficientStock_shouldThrowStockDomainException() {
        // Arrange
        Stock stock = buildExistingStock(5);

        // Act & Assert
        assertThrows(StockDomainException.class, () -> stock.reserve(10));
    }

    @Test
    @DisplayName("Releasing stock should increase the quantity by the given amount")
    void testRelease_withPositiveAmount_shouldIncreaseQuantity() {
        // Arrange
        Stock stock = buildExistingStock(20);

        // Act
        stock.release(15);

        // Assert
        assertEquals(35, stock.getQuantity().getValue());
    }

    private Stock buildExistingStock(int quantity) {
        return Stock.reconstitute(
                new StockId(UUID.randomUUID()),
                UUID.randomUUID(),
                Quantity.of(quantity),
                Instant.now(),
                Instant.now()
        );
    }
}
