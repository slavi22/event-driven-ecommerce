package com.eventdriven.projection.order.application.service.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.port.in.GetAllOrdersQueryUseCase;
import com.eventdriven.projection.order.application.port.out.query.GetAllOrdersQueryPort;
import com.eventdriven.projection.order.application.query.GetAllOrdersQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
class GetAllOrdersQueryService implements GetAllOrdersQueryUseCase {

    private final GetAllOrdersQueryPort getAllOrdersQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<GetOrderResult> getAllOrders(GetAllOrdersQuery query) {
        log.info("Getting all orders for customer {}", query.customerId());
        return getAllOrdersQueryPort.getAllOrders(query.customerId());
    }
}
