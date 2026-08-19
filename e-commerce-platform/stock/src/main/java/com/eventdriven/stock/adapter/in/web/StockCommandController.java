package com.eventdriven.stock.adapter.in.web;

import com.eventdriven.stock.adapter.in.web.swagger.ReplenishStockOperation;
import com.eventdriven.stock.adapter.in.web.dto.request.ReplenishStockRequest;
import com.eventdriven.stock.adapter.in.web.dto.response.ReplenishStockResponse;
import com.eventdriven.stock.application.dto.ReplenishStockResult;
import com.eventdriven.stock.application.port.in.ReplenishStockUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stock Commands", description = "Endpoints for managing product stock levels")
@RestController
@RequestMapping("/api/v1/stocks")
@Validated
@RequiredArgsConstructor
class StockCommandController {

    private final ReplenishStockUseCase replenishStockUseCase;
    private final StockWebMapper stockWebMapper;

    @ReplenishStockOperation
    @PostMapping("/{productId}/replenish")
    public ResponseEntity<ReplenishStockResponse> replenish(
            @Parameter(description = "UUID of the product to replenish", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
            @PathVariable("productId") @org.hibernate.validator.constraints.UUID String productId,
            @Valid @RequestBody ReplenishStockRequest request) {
        ReplenishStockResult result = replenishStockUseCase.replenishStock(
                stockWebMapper.toReplenishStockCommand(productId, request));
        return ResponseEntity.ok(stockWebMapper.toReplenishStockResponse(result));
    }
}
