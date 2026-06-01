package com.eventdriven.stock.domain.entity;

import com.eventdriven.stock.domain.exception.StockDomainException;
import com.eventdriven.stock.domain.valueobject.Quantity;
import com.eventdriven.stock.domain.valueobject.StockId;
import domain.entity.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Stock extends AggregateRoot<StockId> {

    private UUID productId;
    private Quantity quantity;
    private Instant createdAt;
    private Instant updatedAt;

    public static Stock initialize(UUID productId, int quantity) {
        Stock stock = new Stock();
        stock.setId(new StockId(UUID.randomUUID()));
        stock.productId = productId;
        stock.quantity = Quantity.of(quantity);
        stock.createdAt = Instant.now();
        stock.updatedAt = Instant.now();

        stock.validate();

        return stock;
    }

    public static Stock reconstitute(StockId id, UUID productId, Quantity quantity, Instant createdAt, Instant updatedAt) {
        Stock stock = new Stock();
        stock.setId(id);
        stock.productId = productId;
        stock.quantity = quantity;
        stock.createdAt = createdAt;
        stock.updatedAt = updatedAt;
        return stock;
    }

    public void replenish(int amount) {
        this.quantity = this.quantity.add(Quantity.of(amount));
        this.updatedAt = Instant.now();
    }

    public void reserve(int amount) {
        if (this.quantity.getValue() < amount) {
            throw new StockDomainException(
                    "Insufficient stock: requested " + amount + " but only " + this.quantity.getValue() + " available");
        }
        this.quantity = this.quantity.subtract(Quantity.of(amount));
        this.updatedAt = Instant.now();
    }

    public void release(int amount) {
        this.quantity = this.quantity.add(Quantity.of(amount));
        this.updatedAt = Instant.now();
    }

    private void validate() {
        if (this.productId == null) {
            throw new StockDomainException("Product ID cannot be null");
        }
    }

    private Stock() {
    }

    public UUID getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
