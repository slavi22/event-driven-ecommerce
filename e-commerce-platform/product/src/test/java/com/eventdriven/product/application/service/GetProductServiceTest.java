package com.eventdriven.product.application.service;

import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.mapper.ProductMapper;
import com.eventdriven.product.application.port.out.persistence.GetProductPort;
import com.eventdriven.product.application.query.GetProductQuery;
import com.eventdriven.product.domain.entity.Product;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductServiceTest {

    @Mock
    private GetProductPort getProductPort;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private GetProductService getProductService;

    @Test
    @DisplayName("Getting a product with existing product should return product response")
    void testGetProduct_withExistingProduct_shouldReturnProductResponse() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = Product.create("Product Name", "Product Description",
                                         Money.of(new BigDecimal("9.99")), ProductCategory.ELECTRONICS);
        ProductResponse expectedResponse = new ProductResponse(
                "Product Name", "Product Description",
                new BigDecimal("9.99"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, product.getCreatedAt());

        when(getProductPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(expectedResponse);

        // Act
        ProductResponse result = getProductService.getProduct(new GetProductQuery(productId));

        // Assert
        assertEquals(expectedResponse, result);
        verify(getProductPort).getProductByProductId(any(ProductId.class));
        verify(productMapper).toProductResponse(any(Product.class));
    }

    @Test
    @DisplayName("Getting a product with invalid query should throw ProductNotFoundException")
    void testGetProduct_withNonExistingProduct_shouldThrowException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        GetProductQuery query = new GetProductQuery(productId);
        when(getProductPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> getProductService.getProduct(query));
        verify(getProductPort).getProductByProductId(any(ProductId.class));
    }

}
