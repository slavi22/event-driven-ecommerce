package com.eventdriven.projection.payment.application.port.in;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import com.eventdriven.projection.payment.application.query.GetPaymentByOrderIdQuery;

public interface GetPaymentByOrderIdQueryUseCase {
    GetPaymentResult getPaymentByOrderId(GetPaymentByOrderIdQuery query);
}
