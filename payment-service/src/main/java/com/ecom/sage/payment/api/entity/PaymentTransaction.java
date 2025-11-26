package com.ecom.sage.payment.api.entity;


import com.ecom.sage.payment.api.entity.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name="payment_transaction")
public class PaymentTransaction {

    @Id
    private String id;
    @Column(name = "order_id", nullable = false)
    private String orderId;
    @Column(name = "outstanding_amount", nullable = false)
    private BigDecimal outstandingAmt;
    @Column(name = "paid_amount")
    private BigDecimal paidAmt;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(name = "created_at", nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime created_date;
}

