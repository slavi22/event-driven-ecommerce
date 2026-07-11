package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.swagger.CreateProductOperation;
import com.eventdriven.product.adapter.in.web.swagger.DeleteProductOperation;
import com.eventdriven.product.adapter.in.web.swagger.UpdateProductOperation;
import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
import com.eventdriven.product.adapter.in.web.dto.response.UpdateProductResponse;
import com.eventdriven.product.application.command.DeleteProductCommand;
import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.dto.UpdateProductResult;
import com.eventdriven.product.application.port.in.command.CreateProductUseCase;
import com.eventdriven.product.application.port.in.command.DeleteProductUseCase;
import com.eventdriven.product.application.port.in.command.UpdateProductUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Product Commands", description = "Endpoints for creating, updating, and deleting products")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductCommandController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ProductWebMapper productWebMapper;

    @CreateProductOperation
    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductResult result = createProductUseCase.createProduct(productWebMapper.toCreateProductCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(productWebMapper.toCreateProductResponse(result));
    }

    @UpdateProductOperation
    @PutMapping
    public ResponseEntity<UpdateProductResponse> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
        UpdateProductResult result = updateProductUseCase.updateProduct(productWebMapper.toUpdateProductCommand(request));
        return ResponseEntity.ok(productWebMapper.toUpdateProductResponse(result));
    }

    @DeleteProductOperation
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "UUID of the product to delete", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
            @PathVariable("productId") @org.hibernate.validator.constraints.UUID String productId) {
        deleteProductUseCase.deleteProduct(new DeleteProductCommand(UUID.fromString(productId)));
        return ResponseEntity.noContent().build();
    }
}
