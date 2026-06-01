package com.eventdriven.projection.stock.adapter.in.web;

import com.eventdriven.projection.stock.adapter.in.web.dto.response.GetStockResponse;
import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.port.in.GetStockByProductUseCase;
import com.eventdriven.projection.stock.application.query.GetStockQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stocks")
@Validated
@RequiredArgsConstructor
class StockQueryController {

    private final GetStockByProductUseCase getStockByProductUseCase;
    private final StockWebMapper stockWebMapper;

    @GetMapping("/{productId}")
    public ResponseEntity<GetStockResponse> getStock(
            @PathVariable @org.hibernate.validator.constraints.UUID String productId) {
        GetStockResult result = getStockByProductUseCase.getStockByProductId(
                new GetStockQuery(UUID.fromString(productId)));
        return ResponseEntity.ok(stockWebMapper.toGetStockResponse(result));
    }
}
