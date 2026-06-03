package com.eventdriven.projection.order.application.service.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.exception.OrderNotFoundException;
import com.eventdriven.projection.order.application.port.in.GetOrderQueryUseCase;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.query.GetOrderQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
class GetOrderQueryService implements GetOrderQueryUseCase {

    private final GetOrderQueryPort getOrderQueryPort;

    @Override
    @Transactional(readOnly = true)
    public GetOrderResult getOrder(GetOrderQuery query) {
        log.info("Getting order with id: {}", query.orderId());
        return getOrderQueryPort.getOrderById(query.orderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + query.orderId() + " not found!"));
    }
}
