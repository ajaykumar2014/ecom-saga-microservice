package com.ecom.sage.common.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent implements PaymentEvent {
    private String orderId;
    private String productId;
    private BigDecimal outstandingAmt;
    private BigDecimal paidAmt;
    private String transactionId;
    private boolean success;
    private String notes;
}

