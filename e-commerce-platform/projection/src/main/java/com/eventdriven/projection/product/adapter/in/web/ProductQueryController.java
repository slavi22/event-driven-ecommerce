package com.eventdriven.projection.product.adapter.in.web;

import com.eventdriven.projection.product.adapter.in.web.dto.response.GetProductResponse;
import com.eventdriven.projection.product.application.port.in.query.GetAllProductsQueryUseCase;
import com.eventdriven.projection.product.application.port.in.query.GetProductQueryUseCase;
import com.eventdriven.projection.product.application.query.GetProductQuery;
import com.eventdriven.projection.shared.web.swagger.GetAllProductsOperation;
import com.eventdriven.projection.shared.web.swagger.GetProductOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Read-only endpoints for querying product data")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
class ProductQueryController {

    private final GetAllProductsQueryUseCase getAllProductsQueryUseCase;
    private final GetProductQueryUseCase getProductQueryUseCase;
    private final ProductWebMapper productWebMapper;

    @GetAllProductsOperation
    @GetMapping
    public ResponseEntity<List<GetProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productWebMapper.toGetProductResponseList(getAllProductsQueryUseCase.getAllProducts()));
    }

    @GetProductOperation
    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> getProductById(
            @Parameter(description = "UUID of the product", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
            @PathVariable("productId") @org.hibernate.validator.constraints.UUID String productId) {
        return ResponseEntity.ok(productWebMapper.toGetProductResponse(
                getProductQueryUseCase.getProductByProductId(new GetProductQuery(UUID.fromString(productId)))));
    }
}
