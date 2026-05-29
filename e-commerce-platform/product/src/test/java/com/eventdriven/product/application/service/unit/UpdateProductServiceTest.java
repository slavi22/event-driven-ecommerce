package com.eventdriven.product.application.service.unit;

import com.eventdriven.product.application.command.UpdateProductCommand;
import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.application.port.out.persistence.command.UpdateProductPort;
import com.eventdriven.product.application.service.UpdateProductService;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductUpdatedEventPayload;
import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductId;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductServiceTest {

    @Mock
    private GetProductCommandPort getProductCommandPort;
    @Mock
    private UpdateProductPort updateProductPort;
    @Mock
    private ProductApplicationMapper productApplicationMapper;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private UpdateProductService updateProductService;

    @Test
    @DisplayName("Updating a product with valid command should update the product and save the outbox event")
    void testUpdateProduct_withValidCommand_shouldUpdateProductAndSaveOutboxEvent() {
        // Arrange
        Product existingProduct = buildExistingProduct();
        UpdateProductCommand command = new UpdateProductCommand(
                existingProduct.getId().getValue().toString(),
                "New Name", "New Description", new BigDecimal("19.99"), ProductCategory.ELECTRONICS);

        when(getProductCommandPort.getProductByProductId(any(ProductId.class))).thenReturn(existingProduct);
        when(updateProductPort.update(any(Product.class))).thenReturn(existingProduct);

        // Act
        updateProductService.updateProduct(command);

        // Assert
        verify(updateProductPort, times(1)).update(any(Product.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(ProductUpdatedEventPayload.class));
    }

    @Test
    @DisplayName("Updating a product that does not exist should throw ProductNotFoundException")
    void testUpdateProduct_whenProductNotFound_shouldThrowException() {
        // Arrange
        UpdateProductCommand command = new UpdateProductCommand(
                UUID.randomUUID().toString(),
                "New Name", "New Description", new BigDecimal("19.99"), ProductCategory.ELECTRONICS);

        when(getProductCommandPort.getProductByProductId(any(ProductId.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> updateProductService.updateProduct(command));

        verify(updateProductPort, never()).update(any(Product.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Updating a product with invalid command should throw ProductDomainException")
    void testUpdateProduct_withInvalidCommand_shouldThrowException() {
        // Arrange
        Product existingProduct = buildExistingProduct();
        UpdateProductCommand command = new UpdateProductCommand(
                existingProduct.getId().getValue().toString(),
                null, "New Description", new BigDecimal("19.99"), ProductCategory.ELECTRONICS);

        when(getProductCommandPort.getProductByProductId(any(ProductId.class))).thenReturn(existingProduct);

        // Act & Assert
        assertThrows(ProductDomainException.class, () -> updateProductService.updateProduct(command));

        verify(updateProductPort, never()).update(any(Product.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
    }

    private Product buildExistingProduct() {
        return Product.reconstitute(
                new ProductId(UUID.randomUUID()),
                "Old Name",
                "Old Description",
                Money.of(new BigDecimal("9.99")),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                Instant.now(),
                null);
    }
}
