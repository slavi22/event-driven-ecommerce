package com.eventdriven.product.adapter.in.web.swagger;

import com.eventdriven.product.adapter.in.web.dto.response.CreateProductResponse;
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
        summary = "Create a product",
        description = "Creates a new product and initializes its stock. Publishes a ProductCreated event via Kafka."
)
@ApiResponse(responseCode = "201", description = "Product created successfully",
        content = @Content(schema = @Schema(implementation = CreateProductResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
public @interface CreateProductOperation {
}
