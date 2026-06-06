package com.eventdriven.stock.adapter.in.web.swagger;

import com.eventdriven.stock.adapter.in.web.dto.response.ReplenishStockResponse;
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
        summary = "Replenish stock",
        description = "Adds the specified quantity to the current stock of a product."
)
@ApiResponse(responseCode = "200", description = "Stock replenished successfully",
        content = @Content(schema = @Schema(implementation = ReplenishStockResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid product ID or request body", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
public @interface ReplenishStockOperation {
}
