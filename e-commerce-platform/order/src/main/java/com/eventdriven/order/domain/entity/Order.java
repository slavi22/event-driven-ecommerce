package com.eventdriven.order.domain.entity;

import com.eventdriven.order.domain.exception.OrderDomainException;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderId;
import com.eventdriven.order.domain.valueobject.OrderItem;
import com.eventdriven.order.domain.valueobject.OrderStatus;
import domain.entity.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private UUID customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static Order place(UUID customerId, List<OrderItem> items, Money totalAmount) {
        Order order = new Order();
        order.setId(new OrderId(UUID.randomUUID()));
        order.customerId = customerId;
        order.items = List.copyOf(items);
        order.totalAmount = totalAmount;
        order.status = OrderStatus.PENDING;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        order.validate();
        return order;
    }

    public static Order reconstitute(OrderId id, UUID customerId, List<OrderItem> items,
                                     Money totalAmount, OrderStatus status,
                                     Instant createdAt, Instant updatedAt) {
        Order order = new Order();
        order.setId(id);
        order.customerId = customerId;
        order.items = List.copyOf(items);
        order.totalAmount = totalAmount;
        order.status = status;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        return order;
    }

    public void markStockReserved() {
        if (status != OrderStatus.PENDING) {
            throw new OrderDomainException("Cannot mark stock reserved: order is not PENDING");
        }
        this.status = OrderStatus.STOCK_RESERVED;
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        if (status != OrderStatus.STOCK_RESERVED) {
            throw new OrderDomainException("Cannot confirm: order is not in STOCK_RESERVED state");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void startCancellation() {
        if (status != OrderStatus.STOCK_RESERVED) {
            throw new OrderDomainException("Cannot start cancellation: order is not in STOCK_RESERVED state");
        }
        this.status = OrderStatus.CANCELLING;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CANCELLING) {
            throw new OrderDomainException("Cannot cancel: order is in " + status + " state");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    private void validate() {
        if (customerId == null) {
            throw new OrderDomainException("Customer ID cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new OrderDomainException("Order must have at least one item");
        }
        if (totalAmount == null) {
            throw new OrderDomainException("Total amount cannot be null");
        }
    }

    private Order() {
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
