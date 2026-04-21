package com.eventdriven.product.adapter.in.web;

import com.eventdriven.product.application.service.GetProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
class ProductQueryController {

    private final GetProductService getProductService;

    // TODO: implement the query endpoints for getting products, e.g. get all products, get product by id, etc.
}
