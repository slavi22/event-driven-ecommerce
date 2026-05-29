package com.eventdriven.product.application.service;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.in.GetAllProductsQueryUseCase;
import com.eventdriven.product.application.port.out.persistence.query.GetAllProductsQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class GetAllProductsQueryService implements GetAllProductsQueryUseCase {

    private final GetAllProductsQueryPort getAllProductsQueryPort;
    private final ProductApplicationMapper productApplicationMapper;

    @Override
    @Transactional(readOnly = true, transactionManager = "queryTransactionManager")
    public List<CreateProductResult> getAllProducts() {
        return productApplicationMapper.toCreateProductResultList(getAllProductsQueryPort.getAllProducts());
    }
}
