package com.eventdriven.projection.payment.adapter.out.persistence.postgres;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PaymentQueryPersistenceMapper {
    GetPaymentResult toGetPaymentResult(PaymentReadEntity entity);
    PaymentReadEntity toPaymentReadEntity(GetPaymentResult result);
}
