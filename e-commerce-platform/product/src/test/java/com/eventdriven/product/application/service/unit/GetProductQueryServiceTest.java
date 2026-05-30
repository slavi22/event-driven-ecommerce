package com.eventdriven.product.application.service.unit;

import com.eventdriven.product.application.dto.GetProductResult;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.out.persistence.query.GetProductQueryPort;
import com.eventdriven.product.application.query.GetProductQuery;
import com.eventdriven.product.application.service.query.GetProductQueryService;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.product.domain.valueobject.ProductId;
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
class GetProductQueryServiceTest {

    @Mock
    private GetProductQueryPort getProductQueryPort;
    @Mock
    private ProductApplicationMapper productApplicationMapper;

    @InjectMocks
    private GetProductQueryService getProductQueryService;

    @Test
    @DisplayName("Getting a product with existing product should return product response")
    void testGetProduct_withExistingProduct_shouldReturnProductByProductIdResponse() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = Product.create("Product Name", "Product Description",
                                         Money.of(new BigDecimal("9.99")), ProductCategory.ELECTRONICS);
        GetProductResult expectedResponse = new GetProductResult(product.getId().getValue().toString(),
                                                                 "Product Name", "Product Description",
                                                                 new BigDecimal("9.99"), ProductCategory.ELECTRONICS,
                                                                 ProductStatus.ACTIVE, product.getCreatedAt(),
                                                                 product.getUpdatedAt());

        when(getProductQueryPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.of(product));
        when(productApplicationMapper.toGetProductResult(any(Product.class))).thenReturn(expectedResponse);

        // Act
        GetProductResult result = getProductQueryService.getProductByProductId(new GetProductQuery(productId));

        // Assert
        assertEquals(expectedResponse, result);
        verify(getProductQueryPort).getProductByProductId(any(ProductId.class));
        verify(productApplicationMapper).toGetProductResult(any(Product.class));
    }

    @Test
    @DisplayName("Getting a product with invalid query should throw ProductNotFoundException")
    void testGetProduct_withNonExistingProductByProduct_Id_shouldThrowException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        GetProductQuery query = new GetProductQuery(productId);
        when(getProductQueryPort.getProductByProductId(any(ProductId.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> getProductQueryService.getProductByProductId(query));
        verify(getProductQueryPort).getProductByProductId(any(ProductId.class));
    }

}
