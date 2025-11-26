package com.ecom.sage.shipping.api.entity;

import com.ecom.sage.common.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "shipping_order")
public class ShippingOrder {

    @Id
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "OUTSTANDING_AMT", nullable = false)
    private BigDecimal outstandingAmt;

    @Column(name = "TOTAL_AMT", nullable = false)
    private BigDecimal totalAmt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_id",referencedColumnName = "id")
    private List<OrderShippingStatus> orderShippingHistory = new ArrayList<>();
}
