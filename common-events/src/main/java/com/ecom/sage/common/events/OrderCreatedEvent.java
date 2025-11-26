package com.ecom.sage.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent implements RequestEvent {
    private String orderId;
    private Long customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime orderCreatedTS;
    private String notes;
}
