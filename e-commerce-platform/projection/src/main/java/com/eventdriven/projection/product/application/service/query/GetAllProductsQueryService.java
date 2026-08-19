package com.eventdriven.projection.product.application.service.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.port.in.query.GetAllProductsQueryUseCase;
import com.eventdriven.projection.product.application.port.out.persistence.GetAllProductsQueryPort;
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

    @Override
    @Transactional(readOnly = true)
    public List<GetProductResult> getAllProducts() {
        return getAllProductsQueryPort.getAllProducts();
    }
}
