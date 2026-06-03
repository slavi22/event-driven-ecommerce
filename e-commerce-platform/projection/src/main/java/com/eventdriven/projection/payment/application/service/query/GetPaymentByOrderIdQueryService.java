package com.eventdriven.projection.payment.application.service.query;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import com.eventdriven.projection.payment.application.exception.PaymentNotFoundException;
import com.eventdriven.projection.payment.application.port.in.GetPaymentByOrderIdQueryUseCase;
import com.eventdriven.projection.payment.application.port.out.query.GetPaymentQueryPort;
import com.eventdriven.projection.payment.application.query.GetPaymentByOrderIdQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
class GetPaymentByOrderIdQueryService implements GetPaymentByOrderIdQueryUseCase {

    private final GetPaymentQueryPort getPaymentQueryPort;

    @Override
    @Transactional(readOnly = true)
    public GetPaymentResult getPaymentByOrderId(GetPaymentByOrderIdQuery query) {
        log.info("Getting payment for order id: {}", query.orderId());
        return getPaymentQueryPort.getPaymentByOrderId(query.orderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment for order " + query.orderId() + " not found!"));
    }
}
