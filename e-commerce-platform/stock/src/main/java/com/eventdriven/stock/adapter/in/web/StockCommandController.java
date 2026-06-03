package com.eventdriven.stock.adapter.in.web;

import com.eventdriven.stock.adapter.in.web.dto.request.ReplenishStockRequest;
import com.eventdriven.stock.adapter.in.web.dto.response.ReplenishStockResponse;
import com.eventdriven.stock.application.dto.ReplenishStockResult;
import com.eventdriven.stock.application.port.in.ReplenishStockUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stocks")
@Validated
@RequiredArgsConstructor
class StockCommandController {

    private final ReplenishStockUseCase replenishStockUseCase;
    private final StockWebMapper stockWebMapper;

    @PostMapping("/{productId}/replenish")
    public ResponseEntity<ReplenishStockResponse> replenish(
            @PathVariable @org.hibernate.validator.constraints.UUID String productId,
            @Valid @RequestBody ReplenishStockRequest request) {
        ReplenishStockResult result = replenishStockUseCase.replenishStock(
                stockWebMapper.toReplenishStockCommand(productId, request));
        return ResponseEntity.ok(stockWebMapper.toReplenishStockResponse(result));
    }
}
