package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.request.CreateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
import com.eventdriven.product.adapter.in.web.dto.response.UpdateProductResponse;
import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.dto.UpdateProductResult;
import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.application.port.in.UpdateProductUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductWebMapper productWebMapper;

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductResult
                result = createProductUseCase.createProduct(productWebMapper.toCreateProductCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(productWebMapper.toCreateProductResponse(result));
    }

    @PutMapping
    public ResponseEntity<UpdateProductResponse> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
        UpdateProductResult result =
                updateProductUseCase.updateProduct(productWebMapper.toUpdateProductCommand(request));
        return ResponseEntity.ok(productWebMapper.toUpdateProductResponse(result));
    }
}
