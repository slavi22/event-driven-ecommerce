package com.eventdriven.projection.product.application.service.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;

import com.eventdriven.projection.product.application.exception.ProductNotFoundException;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
import com.eventdriven.projection.product.application.query.GetProductQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductQueryServiceTest {

    @Mock
    private GetProductQueryPort getProductQueryPort;

    @InjectMocks
    private GetProductQueryService getProductQueryService;

    @Test
    @DisplayName("Getting a product with existing id should return product result")
    void testGetProduct_withExistingProduct_shouldReturnGetProductResult() {
        UUID productId = UUID.randomUUID();
        GetProductResult expected = new GetProductResult(productId.toString(), "Product Name", "Product Description",
                new BigDecimal("9.99"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, Instant.now(), null);

        when(getProductQueryPort.getProductById(productId)).thenReturn(Optional.of(expected));

        GetProductResult result = getProductQueryService.getProductByProductId(new GetProductQuery(productId));

        assertEquals(expected, result);
        verify(getProductQueryPort).getProductById(productId);
    }

    @Test
    @DisplayName("Getting a product with non-existing id should throw ProductNotFoundException")
    void testGetProduct_withNonExistingProductId_shouldThrowProductNotFoundException() {
        UUID productId = UUID.randomUUID();
        when(getProductQueryPort.getProductById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> getProductQueryService.getProductByProductId(new GetProductQuery(productId)));
        verify(getProductQueryPort).getProductById(productId);
    }
}
