package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
import com.eventdriven.product.adapter.in.web.dto.response.UpdateProductResponse;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.command.DeleteProductCommand;
import com.eventdriven.product.application.command.UpdateProductCommand;
import com.eventdriven.product.application.dto.UpdateProductResult;
import com.eventdriven.product.application.port.in.command.CreateProductUseCase;
import com.eventdriven.product.application.port.in.command.DeleteProductUseCase;
import com.eventdriven.product.application.port.in.command.UpdateProductUseCase;
import com.eventdriven.product.domain.exception.ProductDomainException;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
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
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductCommandController.class)
class ProductCommandControllerTest {

    @MockitoBean
    private CreateProductUseCase createProductUseCase;
    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;
    @MockitoBean
    private DeleteProductUseCase deleteProductUseCase;
    @MockitoBean
    private ProductWebMapper productWebMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    // Create Product Tests

    @Test
    @DisplayName("Creating a product with valid request should return created product")
    void testCreateProduct_withValidRequest_shouldReturnCreatedProduct() throws Exception {
        // Arrange
        CreateProductRequest request =
                new CreateProductRequest("Product Name", "Product Description", new BigDecimal("9.99"),
                                         "ELECTRONICS", 10);

        CreateProductCommand command =
                new CreateProductCommand("Product Name", "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS, 10);

        CreateProductResponse response =
                new CreateProductResponse("Product Name", "Product Description", new BigDecimal("9.99"),
                                          "ELECTRONICS", "ACTIVE", ZonedDateTime.now());

        when(productWebMapper.toCreateProductCommand(any(CreateProductRequest.class))).thenReturn(command);
        when(productWebMapper.toCreateProductResponse(any())).thenReturn(response);

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
                new CreateProductRequest("", "", null, "", null);
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
                                         "ELECTRONICS", 10);
        CreateProductCommand command =
                new CreateProductCommand("Product Name", "Product Description", new BigDecimal("9.99"),
                                         ProductCategory.ELECTRONICS, 10);

        when(productWebMapper.toCreateProductCommand(any(CreateProductRequest.class))).thenReturn(command);
        when(createProductUseCase.createProduct(any(CreateProductCommand.class))).thenThrow(
                new ProductDomainException("error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
    }

    // Update Product Tests

    @Test
    @DisplayName("Updating a product with valid request should return updated product")
    void testUpdateProduct_withValidRequest_shouldReturnUpdatedProduct() throws Exception {
        // Arrange
        UpdateProductRequest request = new UpdateProductRequest(
                UUID.randomUUID().toString(), "Updated Name", "Updated Description",
                new BigDecimal("19.99"), "ELECTRONICS");

        UpdateProductCommand command = new UpdateProductCommand(
                request.productId(), "Updated Name", "Updated Description",
                new BigDecimal("19.99"), ProductCategory.ELECTRONICS);

        UpdateProductResult result = new UpdateProductResult("Updated Name", "Updated Description",
                                                             new BigDecimal("19.99"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE);

        UpdateProductResponse response = new UpdateProductResponse(
                "Updated Name", "Updated Description", new BigDecimal("19.99"), "ELECTRONICS", "ACTIVE");

        when(updateProductUseCase.updateProduct(command)).thenReturn(result);
        when(productWebMapper.toUpdateProductCommand(any(UpdateProductRequest.class))).thenReturn(command);
        when(productWebMapper.toUpdateProductResponse(any(UpdateProductResult.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(response.name()))
               .andExpect(jsonPath("$.description").value(response.description()))
               .andExpect(jsonPath("$.price").value(response.price()));

        verify(updateProductUseCase).updateProduct(any(UpdateProductCommand.class));
    }

    @Test
    @DisplayName("Updating a product with invalid request should return bad request")
    void testUpdateProduct_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductRequest request = new UpdateProductRequest("", "", null, null, "");

        // Act & Assert
        mockMvc.perform(put("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.detail").value("Invalid request body"))
               .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Updating a product when use case throws exception should return internal server error")
    void testUpdateProduct_whenUseCaseThrowsException_shouldReturnInternalServerError() throws Exception {
        // Arrange
        UpdateProductRequest request = new UpdateProductRequest(
                UUID.randomUUID().toString(), "Updated Name", "Updated Description",
                new BigDecimal("19.99"), "ELECTRONICS");

        UpdateProductCommand command = new UpdateProductCommand(
                request.productId(), "Updated Name", "Updated Description",
                new BigDecimal("19.99"), ProductCategory.ELECTRONICS);

        when(productWebMapper.toUpdateProductCommand(any(UpdateProductRequest.class))).thenReturn(command);
        when(updateProductUseCase.updateProduct(any(UpdateProductCommand.class)))
                .thenThrow(new ProductDomainException("error"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products")
                                .content(jsonMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
    }

    // Delete Product Tests

    @Test
    @DisplayName("Deleting a product with a valid product ID should return no content")
    void testDeleteProduct_withValidProductId_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{productId}", UUID.randomUUID()))
               .andExpect(status().isNoContent());

        verify(deleteProductUseCase).deleteProduct(any(DeleteProductCommand.class));
    }

    @Test
    @DisplayName("Deleting a product with an invalid product ID should return bad request")
    void testDeleteProduct_withInvalidProductId_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{productId}", "not-a-uuid"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.detail").value("Invalid request parameter"))
               .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Deleting a product when use case throws exception should return internal server error")
    void testDeleteProduct_whenUseCaseThrowsException_shouldReturnInternalServerError() throws Exception {
        // Arrange
        doThrow(new ProductDomainException("error")).when(deleteProductUseCase).deleteProduct(any(DeleteProductCommand.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{productId}", UUID.randomUUID()))
               .andExpect(status().isInternalServerError());
    }
}
