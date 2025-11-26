package com.ecom.sage.order.api.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    private Long customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerId=" + customerId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}

