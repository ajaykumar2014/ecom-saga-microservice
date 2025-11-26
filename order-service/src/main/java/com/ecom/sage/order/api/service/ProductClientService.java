package com.ecom.sage.order.api.service;

import com.ecom.sage.order.api.client.ProductDetailsClient;
import com.ecom.sage.order.api.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.flogger.FluentLogger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductClientService {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    private final ProductDetailsClient productDetailsClient;
    private final CircuitBreakerRegistry cbRegistry;

    public ProductClientService(ProductDetailsClient client, CircuitBreakerRegistry cbRegistry) {
        this.productDetailsClient = client;
        this.cbRegistry = cbRegistry;
    }

    @CircuitBreaker(name = "productCB", fallbackMethod = "onFailure")
    public Product callProductDetails(String productId) {
        JsonNode productJson = this.productDetailsClient.getProductById(productId);
        if (productJson == null || productJson.isEmpty()) {
            return onFailure(productId, new RuntimeException("Product Details is empty"));
        }
        Product product = new Product();
        product.setProductId(productJson.get("productId").asText());
        product.setProductDetails(productJson.get("productDetails").asText());
        product.setPricePerUnit(BigDecimal.valueOf(productJson.get("pricePerUnit").asDouble()));
        return product;
    }

    public Product onFailure(String productId, Throwable ex) {
        LOGGER.atInfo().log("⚠ Inventory service is unavailable. Please try again later.");
        return new Product(productId, 0, "", new BigDecimal("0.00"));
    }
}

