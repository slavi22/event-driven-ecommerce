package com.eventdriven.product.domain.entity;

import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductId;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    @DisplayName("Marking an active product as inactive should change the product status to INACTIVE")
    void testMarkProductAsInactive_whenReconstitutedProductIsActive_shouldMarkProductAsInactive() {
        // Arrange
        Money reconstitutedMoney = Money.of(new BigDecimal("9.99"));
        ProductId reconstitutedProductId = new ProductId(UUID.randomUUID());
        ProductCategory reconstitutedProductCategory = ProductCategory.ELECTRONICS;
        ProductStatus reconstitutedProductStatus = ProductStatus.ACTIVE;
        Instant reconstitutedCreatedAt = Instant.now().minus(1, ChronoUnit.HOURS);

        Product product = Product.reconstitute(reconstitutedProductId, "Product Name", "desc", reconstitutedMoney, reconstitutedProductCategory, reconstitutedProductStatus, reconstitutedCreatedAt, null);

        // Act
        product.markProductAsInactive();

        // Assert
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }
}
