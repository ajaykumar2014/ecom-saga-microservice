package com.ecom.sage.order.api.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse implements Serializable {
    private Long customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status;
    private String reason;
}

