package com.eventdriven.projection.shared.web.swagger;

import com.eventdriven.projection.stock.adapter.in.web.dto.response.GetStockResponse;
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
@Operation(summary = "Get stock by product ID", description = "Returns the current stock level for a given product from the read model.")
@ApiResponse(responseCode = "200", description = "Stock data found",
        content = @Content(schema = @Schema(implementation = GetStockResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid product ID format", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Stock entry not found for the given product", content = @Content)
public @interface GetStockOperation {
}
