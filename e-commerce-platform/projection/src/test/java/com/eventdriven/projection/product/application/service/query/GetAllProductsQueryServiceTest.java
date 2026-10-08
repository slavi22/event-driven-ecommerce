package com.eventdriven.projection.product.application.service.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;

import com.eventdriven.projection.product.application.port.out.persistence.GetAllProductsQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllProductsQueryServiceTest {

    @Mock
    private GetAllProductsQueryPort getAllProductsQueryPort;

    @InjectMocks
    private GetAllProductsQueryService getAllProductsQueryService;

    @Test
    @DisplayName("Getting all products when products exist should return list of product results")
    void testGetAllProducts_whenProductsExist_shouldReturnGetProductResultList() {
        UUID productId = UUID.randomUUID();
        List<GetProductResult> expected = List.of(
                new GetProductResult(productId.toString(), "Product Name", "Product Description",
                        new BigDecimal("9.99"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                        Instant.now(), null));

        when(getAllProductsQueryPort.getAllProducts()).thenReturn(expected);

        List<GetProductResult> result = getAllProductsQueryService.getAllProducts();

        assertEquals(expected, result);
        verify(getAllProductsQueryPort).getAllProducts();
    }

    @Test
    @DisplayName("Getting all products when no products exist should return empty list")
    void testGetAllProducts_whenNoProductsExist_shouldReturnEmptyList() {
        when(getAllProductsQueryPort.getAllProducts()).thenReturn(List.of());

        List<GetProductResult> result = getAllProductsQueryService.getAllProducts();

        assertTrue(result.isEmpty());
        verify(getAllProductsQueryPort).getAllProducts();
    }
}
