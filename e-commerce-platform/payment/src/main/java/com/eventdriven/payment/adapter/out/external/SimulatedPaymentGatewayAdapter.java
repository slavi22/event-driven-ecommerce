package com.eventdriven.payment.adapter.out.external;

import com.eventdriven.payment.application.port.out.external.PaymentGatewayPort;
import com.eventdriven.payment.application.port.out.external.PaymentGatewayResult;
import com.eventdriven.payment.domain.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public PaymentGatewayResult charge(Payment payment) {
        // won't be implemented
        // 2 cases => success if the order amount is under 10k and failure if the order amount is above 10k
        if (payment.getAmount().getAmount().compareTo(new BigDecimal("10000")) < 0) {
            return new PaymentGatewayResult(true, null);
        } else {
            return new PaymentGatewayResult(false, "Insufficient funds");
        }
    }
}
