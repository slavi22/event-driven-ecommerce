package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCommandController.class)
class ProductCommandControllerTest {

    @MockitoBean
    private CreateProductUseCase createProductUseCase;
    @MockitoBean
    private ProductWebMapper productWebMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Creating a product with valid request should return created product")
    void testCreateProduct_withValidRequest_shouldReturnCreatedProduct() throws Exception {
        // Arrange
        CreateProductRequest request =
                new CreateProductRequest("Product Name", "Product Description", new BigDecimal("9.99"),
                                         "ELECTRONICS");

        CreateProductCommand command =
                new CreateProductCommand("Product Name", "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS);

        when(productWebMapper.toCreateProductCommand(any(CreateProductRequest.class))).thenReturn(command);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value(request.name()))
               .andExpect(jsonPath("$.description").value(request.description()))
               .andExpect(jsonPath("$.price").value(request.price()));

        verify(createProductUseCase).createProduct(any(CreateProductCommand.class));
    }

    @Test
    @DisplayName("Creating a product with invalid request should return bad request")
    void testCreateProduct_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductRequest request =
                new CreateProductRequest("", "", null, "");
        String expectedDetailValue = "Invalid request body";

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.detail").value(expectedDetailValue))
               .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Creating a product when use case throws exception should return internal server error")
    void testCreateProduct_whenUseCaseThrowsException_shouldReturnInternalServerError() throws Exception {
        // Arrange
        CreateProductRequest request =
                new CreateProductRequest("Product Name", "Product Description", new BigDecimal("9.99"),
                                         "ELECTRONICS");
        CreateProductCommand command =
                new CreateProductCommand("Product Name", "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS);

        when(productWebMapper.toCreateProductCommand(any(CreateProductRequest.class))).thenReturn(command);
        when(createProductUseCase.createProduct(any(CreateProductCommand.class))).thenThrow(
                new ProductDomainException("error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
    }
}
