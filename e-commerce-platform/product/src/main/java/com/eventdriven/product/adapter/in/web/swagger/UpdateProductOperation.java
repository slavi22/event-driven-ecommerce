package com.eventdriven.product.adapter.in.web.swagger;

import com.eventdriven.product.adapter.in.web.dto.response.UpdateProductResponse;
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
        summary = "Update a product",
        description = "Updates the name, description, price, and category of an existing product."
)
@ApiResponse(responseCode = "200", description = "Product updated successfully",
        content = @Content(schema = @Schema(implementation = UpdateProductResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
public @interface UpdateProductOperation {
}
