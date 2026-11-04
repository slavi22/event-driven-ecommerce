package com.eventdriven.projection.payment.application.port.out.query;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;

public interface SavePaymentQueryPort {
    GetPaymentResult save(GetPaymentResult result);
}
