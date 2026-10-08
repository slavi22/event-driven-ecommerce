package com.eventdriven.projection.shared.web.swagger;

import com.eventdriven.projection.order.adapter.in.web.dto.response.GetOrderResponse;
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
@Operation(summary = "Get order by ID", description = "Returns the current state of a single order from the read model.")
@ApiResponse(responseCode = "200", description = "Order found",
        content = @Content(schema = @Schema(implementation = GetOrderResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid order ID format", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
public @interface GetOrderOperation {
}
