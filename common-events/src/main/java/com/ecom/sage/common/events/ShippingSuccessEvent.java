package com.ecom.sage.common.events;

import com.ecom.sage.common.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingSuccessEvent implements ShippingEvent {
    private String shippingId;
    private String orderId;
    private String productId;
    private String transactionId;
    private BigDecimal paidAmt;
    private BigDecimal outstandingAmt;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String notes;

    @Override
    public String getOrderId() {
        return orderId;
    }

    @Override
    public String getShippingId() {
        return shippingId;
    }
}
