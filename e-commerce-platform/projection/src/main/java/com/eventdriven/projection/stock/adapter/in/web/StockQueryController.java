package com.eventdriven.projection.stock.adapter.in.web;

import com.eventdriven.projection.shared.web.swagger.GetStockOperation;
import com.eventdriven.projection.stock.adapter.in.web.dto.response.GetStockResponse;
import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.port.in.GetStockByProductUseCase;
import com.eventdriven.projection.stock.application.query.GetStockQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Stocks", description = "Read-only endpoints for querying stock levels")
@RestController
@RequestMapping("/api/v1/stocks")
@Validated
@RequiredArgsConstructor
class StockQueryController {

    private final GetStockByProductUseCase getStockByProductUseCase;
    private final StockWebMapper stockWebMapper;

    @GetStockOperation
    @GetMapping("/{productId}")
    public ResponseEntity<GetStockResponse> getStock(
            @Parameter(description = "UUID of the product to look up stock for", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
            @PathVariable("productId") @org.hibernate.validator.constraints.UUID String productId) {
        GetStockResult result = getStockByProductUseCase.getStockByProductId(
                new GetStockQuery(UUID.fromString(productId)));
        return ResponseEntity.ok(stockWebMapper.toGetStockResponse(result));
    }
}
