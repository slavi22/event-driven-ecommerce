package com.eventdriven.product.application.service.unit;

import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.application.port.out.persistence.command.SaveProductPort;
import com.eventdriven.product.application.service.CreateProductService;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductCreatedEventPayload;
import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private SaveProductPort saveProductPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private ProductApplicationMapper productApplicationMapper;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private CreateProductService createProductService;

    @Test
    @DisplayName("Creating a product with valid command should create the product successfully")
    void testCreateProduct_withValidCommand_shouldCreateProductSuccessfully() {
        // Arrange
        CreateProductCommand command =
                new CreateProductCommand("Product Name", "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS, 10);

        // Act
        createProductService.createProduct(command);

        // Assert
        verify(saveProductPort, times(1)).save(any(Product.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(ProductCreatedEventPayload.class));
    }

    @Test
    @DisplayName("Creating a product with invalid command should throw exception")
    void testCreateProduct_withInvalidCommand_shouldThrowException() {
        // Arrange
        CreateProductCommand command =
                new CreateProductCommand(null, "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS, 10);

        // Act
        assertThrows(ProductDomainException.class, () -> createProductService.createProduct(command));

        // Assert
        verify(saveProductPort, never()).save(any(Product.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
        verify(jsonMapper, never()).writeValueAsString(any(ProductCreatedEventPayload.class));
    }
}
