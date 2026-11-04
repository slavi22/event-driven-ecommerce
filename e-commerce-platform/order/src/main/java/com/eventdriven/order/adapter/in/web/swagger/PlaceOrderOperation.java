package com.eventdriven.order.adapter.in.web.swagger;

import com.eventdriven.order.adapter.in.web.dto.response.PlaceOrderResponse;
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
@Operation(
        summary = "Place an order",
        description = "Places a new order for the authenticated customer. Initiates the order saga: stock reservation → payment processing → order confirmation."
)
@ApiResponse(responseCode = "201", description = "Order placed successfully",
        content = @Content(schema = @Schema(implementation = PlaceOrderResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized — X-User-Id header missing or invalid", content = @Content)
public @interface PlaceOrderOperation {
}
