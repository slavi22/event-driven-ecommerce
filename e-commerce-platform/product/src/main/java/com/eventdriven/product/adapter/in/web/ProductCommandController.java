package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.application.service.CreateProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
class ProductCommandController {
    private final CreateProductUseCase createProductUseCase;

    // TODO: implement the create product endpoint
}
