package com.eventdriven.product.application.service.query;

import com.eventdriven.product.application.dto.GetProductResult;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.in.query.GetAllProductsQueryUseCase;
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
    public List<GetProductResult> getAllProducts() {
        return productApplicationMapper.toGetProductResultList(getAllProductsQueryPort.getAllProducts());
    }
}
