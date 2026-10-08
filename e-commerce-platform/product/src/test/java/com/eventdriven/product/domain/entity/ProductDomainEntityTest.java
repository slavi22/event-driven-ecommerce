package com.eventdriven.product.domain.entity;

import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductDomainEntityTest {

    @Test
    @DisplayName("Creating a product with a blank name should throw ProductDomainException")
    void testCreateProduct_withBlankName_shouldThrowProductDomainException() {
        // Arrange
        Money money = Money.of(new BigDecimal("9.99"));

        // Act & Assert
        assertThrows(ProductDomainException.class, () -> Product.create("", "desc", money, ProductCategory.ELECTRONICS));
    }

    @Test
    @DisplayName("Creating a product with a null price should throw ProductDomainException")
    void testCreateProduct_withNullPrice_shouldThrowProductDomainException() {
        // Act & Assert
        assertThrows(ProductDomainException.class, () -> Product.create("Product Name", "desc", null, ProductCategory.ELECTRONICS));
    }

    @Test
    @DisplayName("Creating a product with valid input should create the product successfully")
    void testCreateProduct_withValidInput_shouldCreateProduct() {
        // Arrange
        Money money = Money.of(new BigDecimal("9.99"));

        // Act
        Product product = Product.create("Product Name", "desc", money, ProductCategory.ELECTRONICS);

        // Assert
        assertEquals("Product Name", product.getName());
        assertEquals("desc", product.getDescription());
        assertEquals(money, product.getPrice());
        assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
    }

    @Test
    @DisplayName("Updating a product with valid input should update the product fields")
    void testUpdateProduct_withValidInput_shouldUpdateProductFields() {
        // Arrange
        Product product = buildExistingProduct();
        Money newPrice = Money.of(new BigDecimal("19.99"));

        // Act
        product.update("New Name", "New Description", newPrice, ProductCategory.CLOTHING);

        // Assert
        assertEquals("New Name", product.getName());
        assertEquals("New Description", product.getDescription());
        assertEquals(newPrice, product.getPrice());
        assertEquals(ProductCategory.CLOTHING, product.getCategory());
    }

    @Test
    @DisplayName("Updating a product with a blank name should throw ProductDomainException")
    void testUpdateProduct_withBlankName_shouldThrowProductDomainException() {
        // Arrange
        Product product = buildExistingProduct();
        Money price = Money.of(new BigDecimal("19.99"));

        // Act & Assert
        assertThrows(ProductDomainException.class,
                () -> product.update("", "New Description", price, ProductCategory.ELECTRONICS));
    }

    @Test
    @DisplayName("Deleting a product should set its status to INACTIVE")
    void testDeleteProduct_shouldSetStatusToInactive() {
        // Arrange
        Product product = buildExistingProduct();

        // Act
        product.delete();

        // Assert
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }

    private Product buildExistingProduct() {
        return Product.reconstitute(
                new ProductId(UUID.randomUUID()),
                "Original Name",
                "Original Description",
                Money.of(new BigDecimal("9.99")),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                Instant.now(),
                null);
    }
}
