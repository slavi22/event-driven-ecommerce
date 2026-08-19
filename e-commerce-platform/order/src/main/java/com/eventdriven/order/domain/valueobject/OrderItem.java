package com.eventdriven.order.domain.valueobject;

import com.eventdriven.order.domain.exception.OrderDomainException;

import java.util.Objects;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;

    public static OrderItem of(UUID productId, int quantity, Money unitPrice) {
        if (productId == null) {
            throw new OrderDomainException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new OrderDomainException("Quantity must be positive");
        }
        if (unitPrice == null) {
            throw new OrderDomainException("Unit price cannot be null");
        }
        return new OrderItem(productId, quantity, unitPrice);
    }

    private OrderItem(UUID productId, int quantity, Money unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity && Objects.equals(productId, orderItem.productId) &&
               Objects.equals(unitPrice, orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}
