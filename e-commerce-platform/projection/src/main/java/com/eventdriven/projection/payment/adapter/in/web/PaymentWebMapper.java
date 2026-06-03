package com.eventdriven.projection.payment.adapter.in.web;

import com.eventdriven.projection.payment.adapter.in.web.dto.response.GetPaymentResponse;
import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PaymentWebMapper {
    GetPaymentResponse toGetPaymentResponse(GetPaymentResult result);
}
