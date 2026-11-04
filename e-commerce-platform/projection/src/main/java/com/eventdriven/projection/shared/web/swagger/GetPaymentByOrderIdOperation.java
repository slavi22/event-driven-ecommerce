package com.eventdriven.projection.shared.web.swagger;

import com.eventdriven.projection.payment.adapter.in.web.dto.response.GetPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get payment by order ID", description = "Returns the payment details for a given order from the read model.")
@ApiResponse(responseCode = "200", description = "Payment found",
        content = @Content(schema = @Schema(implementation = GetPaymentResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid order ID format", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Payment not found for the given order", content = @Content)
public @interface GetPaymentByOrderIdOperation {
}
