package com.eventdriven.product.adapter.in.web.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Delete a product",
        description = "Soft-deletes a product by its UUID. Publishes a ProductDeleted event via Kafka."
)
@ApiResponse(responseCode = "204", description = "Product deleted successfully", content = @Content)
@ApiResponse(responseCode = "400", description = "Invalid product ID format", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
@ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
public @interface DeleteProductOperation {
}
