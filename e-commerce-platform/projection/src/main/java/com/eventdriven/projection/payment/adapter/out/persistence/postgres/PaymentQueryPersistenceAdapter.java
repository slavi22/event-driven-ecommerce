package com.eventdriven.projection.payment.adapter.out.persistence.postgres;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import com.eventdriven.projection.payment.application.port.out.query.GetPaymentQueryPort;
import com.eventdriven.projection.payment.application.port.out.query.SavePaymentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PaymentQueryPersistenceAdapter implements GetPaymentQueryPort, SavePaymentQueryPort {

    private final PaymentReadJpaRepository paymentReadJpaRepository;
    private final PaymentQueryPersistenceMapper paymentQueryPersistenceMapper;

    @Override
    public Optional<GetPaymentResult> getPaymentByOrderId(UUID orderId) {
        return paymentReadJpaRepository.findById(orderId)
                .map(paymentQueryPersistenceMapper::toGetPaymentResult);
    }

    @Override
    public GetPaymentResult save(GetPaymentResult result) {
        PaymentReadEntity entity = paymentQueryPersistenceMapper.toPaymentReadEntity(result);
        return paymentQueryPersistenceMapper.toGetPaymentResult(paymentReadJpaRepository.save(entity));
    }
}
