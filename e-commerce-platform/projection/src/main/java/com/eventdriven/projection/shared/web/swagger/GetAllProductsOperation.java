package com.eventdriven.projection.shared.web.swagger;

import com.eventdriven.projection.product.adapter.in.web.dto.response.GetProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get all products", description = "Returns all products from the read model.")
@ApiResponse(responseCode = "200", description = "List of products",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetProductResponse.class))))
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
public @interface GetAllProductsOperation {
}
