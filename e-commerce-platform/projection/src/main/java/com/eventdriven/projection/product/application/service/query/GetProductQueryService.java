package com.eventdriven.projection.product.application.service.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.exception.ProductNotFoundException;
import com.eventdriven.projection.product.application.port.in.query.GetProductQueryUseCase;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
import com.eventdriven.projection.product.application.query.GetProductQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class GetProductQueryService implements GetProductQueryUseCase {

    private final GetProductQueryPort getProductQueryPort;

    @Override
    @Transactional(readOnly = true)
    public GetProductResult getProductByProductId(GetProductQuery query) {
        log.info("Getting product with id: {}", query.productId());
        return getProductQueryPort.getProductById(query.productId()).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id: " + query.productId()));
    }
}
