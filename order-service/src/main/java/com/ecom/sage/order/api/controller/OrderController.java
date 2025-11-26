package com.ecom.sage.order.api.controller;

import com.ecom.sage.order.api.model.CreateOrderRequest;
import com.ecom.sage.order.api.response.OrderResponse;
import com.ecom.sage.order.api.service.OrderService;
import com.google.common.flogger.FluentLogger;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")

public class OrderController {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Bulkhead(name = "orderServiceBulkhead",type = Bulkhead.Type.SEMAPHORE)
    @RateLimiter(name = "orderRateLimiter")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) throws InterruptedException {
        LOGGER.atInfo().log("✅ Received a CreateOrderRequest %s", req);
        return ResponseEntity.ok(orderService.createOrder(req));
    }

    @GetMapping("/{orderId}")
    public OrderResponse fetchFullOrder(@PathVariable String orderId) throws InterruptedException {
        LOGGER.atInfo().log("✅ Fetching Order Details by orderid %s", orderId);
        return orderService.getOrderByOrderId(orderId);
    }
}
