package com.ecom.sage.order.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service-api",url = "http://localhost:9097",configuration = FeignConfig.class)
public interface ProductDetailsClient {
    @GetMapping(value="/product/{productId}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getProductById(@PathVariable("productId") String productId);
}
