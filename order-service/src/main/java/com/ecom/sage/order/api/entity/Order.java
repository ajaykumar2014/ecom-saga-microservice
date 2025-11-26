package com.ecom.sage.order.api.entity;

import com.ecom.sage.common.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "product_id", nullable = false)
    private String productId;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "shipping_id")
    private String shippingId;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private List<OrderStatusDetail> orderStatusDetails = new ArrayList<>();
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime created_date;
}

