package com.eventdriven.product.application.service;

import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.mapper.ProductMapper;
import com.eventdriven.product.application.port.in.GetProductUseCase;
import com.eventdriven.product.application.port.out.persistence.GetProductPort;
import com.eventdriven.product.application.query.GetProductQuery;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Log4j2
@Service
@Validated
@RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {
    private final GetProductPort getProductPort;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(GetProductQuery query) {
        log.info("Getting product with id: {}", query.productId());
        Product product = getProductPort.getProductByProductId(new ProductId(query.productId())).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id: " + query.productId()));
        log.info("Product found with id: {} returning response", query.productId());
        return productMapper.toProductResponse(product);
    }
}
