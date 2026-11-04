package com.eventdriven.product.application.service.unit;

import com.eventdriven.product.application.command.DeleteProductCommand;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.port.out.persistence.command.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.application.service.command.DeleteProductService;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.contracts.product.event.ProductDeletedEventPayload;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    @Mock
    private GetProductCommandPort getProductCommandPort;
    @Mock
    private DeleteProductPort deleteProductPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private DeleteProductService deleteProductService;

    @Test
    @DisplayName("Deleting a product with valid command should delete product and save outbox event")
    void testDeleteProduct_withValidCommand_shouldDeleteProductAndSaveOutboxEvent() {
        // Arrange
        Product existingProduct = buildExistingProduct();
        DeleteProductCommand command = new DeleteProductCommand(existingProduct.getId().getValue());

        when(getProductCommandPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.of(existingProduct));

        // Act
        deleteProductService.deleteProduct(command);

        // Assert
        verify(deleteProductPort, times(1)).deleteProductById(any(ProductId.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(ProductDeletedEventPayload.class));
    }

    @Test
    @DisplayName("Deleting a product that does not exist should throw ProductNotFoundException")
    void testDeleteProduct_whenProductNotFound_shouldThrowProductNotFoundException() {
        // Arrange
        DeleteProductCommand command = new DeleteProductCommand(UUID.randomUUID());

        when(getProductCommandPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> deleteProductService.deleteProduct(command));

        verify(deleteProductPort, never()).deleteProductById(any(ProductId.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
    }

    private Product buildExistingProduct() {
        return Product.reconstitute(
                new ProductId(UUID.randomUUID()),
                "Existing Product",
                "Some description",
                Money.of(new BigDecimal("9.99")),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                Instant.now(),
                null);
    }
}
