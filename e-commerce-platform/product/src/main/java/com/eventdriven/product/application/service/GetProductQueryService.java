package com.eventdriven.product.application.service;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.exception.ProductNotFoundException;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.in.GetProductQueryUseCase;
import com.eventdriven.product.application.port.out.persistence.query.GetProductQueryPort;
import com.eventdriven.product.application.query.GetProductQuery;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class GetProductQueryService implements GetProductQueryUseCase {
    private final GetProductQueryPort getProductQueryPort;
    private final ProductApplicationMapper productApplicationMapper;

    @Override
    @Transactional(readOnly = true, transactionManager = "queryTransactionManager") // we need to specify the transaction manager here since we have marked the command transaction manager as primary bean
    public CreateProductResult getProductByProductId(GetProductQuery query) {
        log.info("Getting product with id: {}", query.productId());
        Product product = getProductQueryPort.getProductByProductId(new ProductId(query.productId())).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id: " + query.productId()));
        log.info("Product found with id: {} returning response", query.productId());

        return productApplicationMapper.toCreateProductResult(product);
    }
}
