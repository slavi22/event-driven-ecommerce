package com.eventdriven.order.domain.entity;

import com.eventdriven.order.domain.exception.OrderDomainException;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderItem;
import com.eventdriven.order.domain.valueobject.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainEntityTest {

    @Test
    @DisplayName("Order.place should create an order with PENDING status and a generated ID")
    void testPlace_shouldCreatePendingOrder() {
        Order order = buildPendingOrder();

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getId());
        assertNotNull(order.getId().getValue());
    }

    @Test
    @DisplayName("Order.place with null customerId should throw OrderDomainException")
    void testPlace_withNullCustomerId_shouldThrowException() {
        // Arrange
        List<OrderItem> list = List.of(buildItem());
        Money money = Money.of(BigDecimal.TEN);

        // Act & Assert
        assertThrows(OrderDomainException.class, () -> Order.place(null, list, money));
    }

    @Test
    @DisplayName("Order.place with empty items list should throw OrderDomainException")
    void testPlace_withEmptyItems_shouldThrowException() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Money totalAmount = Money.of(BigDecimal.TEN);

        // Act & Assert
        assertThrows(OrderDomainException.class,
                     () -> Order.place(customerId, List.of(), totalAmount));
    }

    @Test
    @DisplayName("Order.place with null totalAmount should throw OrderDomainException")
    void testPlace_withNullTotalAmount_shouldThrowException() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        List<OrderItem> items = List.of(buildItem());

        // Act & Assert
        assertThrows(OrderDomainException.class,
                     () -> Order.place(customerId, items, null));
    }

    @Test
    @DisplayName("markStockReserved on PENDING order should transition to STOCK_RESERVED")
    void testMarkStockReserved_fromPending_shouldTransitionToStockReserved() {
        // Arrange
        Order order = buildPendingOrder();

        // Act
        order.markStockReserved();

        // Assert
        assertEquals(OrderStatus.STOCK_RESERVED, order.getStatus());
    }

    @Test
    @DisplayName("markStockReserved on non-PENDING order should throw OrderDomainException")
    void testMarkStockReserved_fromNonPending_shouldThrowException() {
        // Arrange
        Order order = buildPendingOrder();
        order.markStockReserved();

        // Act & Assert
        assertThrows(OrderDomainException.class, order::markStockReserved);
    }

    @Test
    @DisplayName("confirm on STOCK_RESERVED order should transition to CONFIRMED")
    void testConfirm_fromStockReserved_shouldTransitionToConfirmed() {
        // Arrange
        Order order = buildPendingOrder();
        order.markStockReserved();

        // Act
        order.confirm();

        // Assert
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("confirm on PENDING order should throw OrderDomainException")
    void testConfirm_fromPending_shouldThrowException() {
        // Arrange
        Order order = buildPendingOrder();

        // Act & Assert
        assertThrows(OrderDomainException.class, order::confirm);
    }

    @Test
    @DisplayName("startCancellation on STOCK_RESERVED order should transition to CANCELLING")
    void testStartCancellation_fromStockReserved_shouldTransitionToCancelling() {
        // Arrange
        Order order = buildPendingOrder();
        order.markStockReserved();

        // Act
        order.startCancellation();

        // Assert
        assertEquals(OrderStatus.CANCELLING, order.getStatus());
    }

    @Test
    @DisplayName("startCancellation on PENDING order should throw OrderDomainException")
    void testStartCancellation_fromPending_shouldThrowException() {
        // Arrange
        Order order = buildPendingOrder();

        // Act & Assert
        assertThrows(OrderDomainException.class, order::startCancellation);
    }

    @Test
    @DisplayName("cancel on PENDING order should transition to CANCELLED")
    void testCancel_fromPending_shouldTransitionToCancelled() {
        // Arrange
        Order order = buildPendingOrder();

        // Act
        order.cancel();

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("cancel on CANCELLING order should transition to CANCELLED")
    void testCancel_fromCancelling_shouldTransitionToCancelled() {
        // Arrange
        Order order = buildPendingOrder();
        order.markStockReserved();
        order.startCancellation();

        // Act
        order.cancel();

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("cancel on CONFIRMED order should throw OrderDomainException")
    void testCancel_fromConfirmed_shouldThrowException() {
        // Arrange
        Order order = buildPendingOrder();
        order.markStockReserved();
        order.confirm();

        // Act & Assert
        assertThrows(OrderDomainException.class, order::cancel);
    }

    private OrderItem buildItem() {
        return OrderItem.of(UUID.randomUUID(), 2, Money.of(new BigDecimal("10.00")));
    }

    private Order buildPendingOrder() {
        return Order.place(UUID.randomUUID(), List.of(buildItem()), Money.of(new BigDecimal("20.00")));
    }
}
