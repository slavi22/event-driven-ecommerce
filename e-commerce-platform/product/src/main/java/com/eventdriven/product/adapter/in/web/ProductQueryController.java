package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.port.in.GetAllProductsQueryUseCase;
import com.eventdriven.product.application.port.in.GetProductQueryUseCase;
import com.eventdriven.product.application.query.GetProductQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
class ProductQueryController {

    private final GetAllProductsQueryUseCase getAllProductsQueryUseCase;
    private final GetProductQueryUseCase getProductQueryUseCase;

    @GetMapping
    public ResponseEntity<List<CreateProductResult>> getAllProducts() {
        return ResponseEntity.ok(getAllProductsQueryUseCase.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<CreateProductResult> getProductById(
            @PathVariable @org.hibernate.validator.constraints.UUID String productId) {
        CreateProductResult response =
                getProductQueryUseCase.getProductByProductId(new GetProductQuery(UUID.fromString(productId)));
        return ResponseEntity.ok(response);
    }
}
