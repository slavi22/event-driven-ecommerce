package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.adapter.in.web.dto.response.GetProductResponse;
import com.eventdriven.product.application.port.in.query.GetAllProductsQueryUseCase;
import com.eventdriven.product.application.port.in.query.GetProductQueryUseCase;
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
    private final ProductWebMapper productWebMapper;

    @GetMapping
    public ResponseEntity<List<GetProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productWebMapper.toGetProductResponseList(getAllProductsQueryUseCase.getAllProducts()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> getProductById(
            @PathVariable("productId") @org.hibernate.validator.constraints.UUID String productId) {
        return ResponseEntity.ok(productWebMapper.toGetProductResponse(
                getProductQueryUseCase.getProductByProductId(new GetProductQuery(UUID.fromString(productId)))));
    }
}
