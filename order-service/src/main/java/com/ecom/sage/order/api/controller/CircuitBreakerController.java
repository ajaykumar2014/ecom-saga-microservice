package com.ecom.sage.order.api.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CircuitBreakerController {

    private final CircuitBreakerRegistry cbRegistry;

    public CircuitBreakerController(CircuitBreakerRegistry cbRegistry) {
        this.cbRegistry = cbRegistry;
    }

    @GetMapping("/circuit-state")
    public String cbState() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("productCB");
        return cb.getState().name();
    }

}
