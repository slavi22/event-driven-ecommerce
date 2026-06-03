package com.eventdriven.payment.adapter.out.persistence.command.postgres;

import com.eventdriven.payment.domain.entity.Payment;
import com.eventdriven.payment.domain.valueobject.Money;
import com.eventdriven.payment.domain.valueobject.PaymentId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PaymentPersistenceMapper {

    default PaymentEntity toPaymentEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId().getValue());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount().getAmount());
        entity.setStatus(payment.getStatus());
        entity.setFailureReason(payment.getFailureReason());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    default Payment toPayment(PaymentEntity entity) {
        return Payment.reconstitute(
                new PaymentId(entity.getId()),
                entity.getOrderId(),
                Money.of(entity.getAmount()),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
