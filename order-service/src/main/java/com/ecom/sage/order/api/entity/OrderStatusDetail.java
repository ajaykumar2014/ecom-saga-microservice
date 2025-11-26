package com.ecom.sage.order.api.entity;

import com.ecom.sage.common.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders_status_details")
public class OrderStatusDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reason", nullable = false)
    private String reason = "";
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private com.ecom.sage.common.entity.OrderStatus status;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime created_date = LocalDateTime.now();

    public OrderStatusDetail() {
        super();
    }

    public OrderStatusDetail(OrderStatus status, String reason) {
        super();
        this.status = status;
        this.reason = reason == null ? "" : reason;
        this.created_date = LocalDateTime.now();
    }
}

