package com.eventdriven.product.domain.entity;

import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductId;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import domain.entity.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Product extends AggregateRoot<ProductId> {
    private String name;
    private String description;
    private Money price;
    private ProductStatus status;
    private ProductCategory category;
    private Instant createdAt;
    private Instant updatedAt;

    public static Product create(String name, String description, Money price, ProductCategory category) {
        Product product = new Product();
        product.setId(new ProductId(UUID.randomUUID()));
        product.name = name;
        product.description = description;
        product.price = price;
        product.category = category;
        product.status = ProductStatus.ACTIVE;
        product.createdAt = Instant.now();
        product.updatedAt = Instant.now();

        product.validate();

        // if i do add a list of domain events, i can add a ProductCreatedEvent here

        return product;
    }

    public static Product reconstitute(ProductId id, String name, String description,
                                       Money price, ProductCategory category,
                                       ProductStatus status, Instant createdAt, Instant updatedAt) {
        Product product = new Product();
        product.setId(id);
        product.name = name;
        product.description = description;
        product.price = price;
        product.category = category;
        product.status = status;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        return product;
    }

    private Product() {
    }

    public void update(String name, String description, Money price, ProductCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.updatedAt = Instant.now();
        validate();
    }

    /*
    TODO: this will be used if i want granular methods to update the product,
     instead of just a single updateProduct method that takes an UpdateProductCommand

    public void updatePrice(Money newPrice) {
        this.price = newPrice;
        this.updatedAt = Instant.now();

        // if i do add a list of domain events, i can add a ProductPriceUpdatedEvent here
    }

    public void markProductAsInactive() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();

        // if i do add a list of domain events, i can add a ProductOutOfStockEvent here
    }

    public void markProductAsActive() {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();

        // if i do add a list of domain events, i can add a ProductRestockedEvent here
    }

    public void rename(String newName) {
        this.name = newName;
        this.updatedAt = Instant.now();

        // if i do add a list of domain events, i can add a ProductRenamedEvent here
    }*/

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new ProductDomainException("Product name cannot be blank");
        }
        if (category == null) {
            throw new ProductDomainException("Category is required");
        }
        if (price == null) {
            throw new ProductDomainException("Price is required");
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
