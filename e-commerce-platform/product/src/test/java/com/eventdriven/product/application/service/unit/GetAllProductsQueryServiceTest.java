package com.eventdriven.product.application.service.unit;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.out.persistence.query.GetAllProductsQueryPort;
import com.eventdriven.product.application.service.GetAllProductsQueryService;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllProductsQueryServiceTest {

    @Mock
    private GetAllProductsQueryPort getAllProductsQueryPort;
    @Mock
    private ProductApplicationMapper productApplicationMapper;

    @InjectMocks
    private GetAllProductsQueryService getAllProductsQueryService;

    @Test
    @DisplayName("Getting all products when products exist should return list of product responses")
    void testGetAllProducts_whenProductsExist_shouldReturnProductResponseList() {
        // Arrange
        Product product = Product.create("Product Name", "Product Description",
                                         Money.of(new BigDecimal("9.99")), ProductCategory.ELECTRONICS);
        List<Product> products = List.of(product);
        List<CreateProductResult> expectedResponses = List.of(
                new CreateProductResult("Product Name", "Product Description",
                                        new BigDecimal("9.99"), ProductCategory.ELECTRONICS,
                                        ProductStatus.ACTIVE, product.getCreatedAt()));

        when(getAllProductsQueryPort.getAllProducts()).thenReturn(products);
        when(productApplicationMapper.toCreateProductResultList(anyList())).thenReturn(expectedResponses);

        // Act
        List<CreateProductResult> result = getAllProductsQueryService.getAllProducts();

        // Assert
        assertEquals(expectedResponses, result);
        verify(getAllProductsQueryPort).getAllProducts();
        verify(productApplicationMapper).toCreateProductResultList(products);
    }

    @Test
    @DisplayName("Getting all products when no products exist should return empty list")
    void testGetAllProducts_whenNoProductsExist_shouldReturnEmptyList() {
        // Arrange
        when(getAllProductsQueryPort.getAllProducts()).thenReturn(List.of());
        when(productApplicationMapper.toCreateProductResultList(anyList())).thenReturn(List.of());

        // Act
        List<CreateProductResult> result = getAllProductsQueryService.getAllProducts();

        // Assert
        assertTrue(result.isEmpty());
        verify(getAllProductsQueryPort).getAllProducts();
        verify(productApplicationMapper).toCreateProductResultList(List.of());
    }
}
