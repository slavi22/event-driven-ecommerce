package com.eventdriven.payment.adapter.out.persistence.command.postgres;

import com.eventdriven.payment.application.port.out.command.GetPaymentPort;
import com.eventdriven.payment.application.port.out.command.SavePaymentPort;
import com.eventdriven.payment.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PaymentPersistenceAdapter implements SavePaymentPort, GetPaymentPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentPersistenceMapper paymentPersistenceMapper;

    @Override
    public void save(Payment payment) {
        paymentJpaRepository.save(paymentPersistenceMapper.toPaymentEntity(payment));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                                   .map(paymentPersistenceMapper::toPayment);
    }
}
