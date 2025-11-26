package com.ecom.sage.shipping.api.controller;

import com.ecom.sage.shipping.api.entity.ShippingOrder;
import com.ecom.sage.shipping.api.repo.ShippingRepository;
import com.ecom.sage.shipping.api.service.ShippingService;
import com.google.common.flogger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shipping")
public class ShippingOrderController {

    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    private final List<String> listOfStatus = List.of("SHIPPED",
            "SHIPPED_READY",
            "SHIPPED_CANCELLED",
            "SHIPMENT_FAILED");
    @Autowired
    private ShippingService shippingService;
    @Autowired
    private ShippingRepository repository;

    @GetMapping("/{shippingId}/{status}")
    public ResponseEntity<?> getUpdateShippingStatus(@PathVariable String shippingId, @PathVariable String status) {
        if (!listOfStatus.contains(status.toUpperCase())) {
            return ResponseEntity.ok("status not found, pls try with below shipping status - " + listOfStatus);
        }
        ShippingOrder shippingOrder = shippingService.doUpdateShippingStatus(shippingId,status);
        return ResponseEntity.ok(shippingOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getUpdateShippingStatus(@PathVariable String orderId) {
        ShippingOrder shippingOrder = repository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Order id not found - " + orderId));
        return ResponseEntity.ok(shippingOrder);
    }
}
