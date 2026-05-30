package com.eventdriven.product.adapter.in.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ProblemDetail handleException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleInvalidRequestBody(MethodArgumentNotValidException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body");
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("errors", e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleInvalidRequestParam(ConstraintViolationException e) {
        // we need a separate exception handler because path variable and request parameter validation errors are wrapped in ConstraintViolationException, not MethodArgumentNotValidException
        // => https://reflectoring.io/bean-validation-with-spring-boot/#:~:text=In%20contrast%20to,Internal%20Server%20Error).
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameter");
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("errors", e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList());
        return problemDetail;
    }
}
