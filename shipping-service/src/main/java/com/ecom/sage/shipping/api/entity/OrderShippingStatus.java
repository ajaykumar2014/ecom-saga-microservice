package com.ecom.sage.shipping.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_shipping_status")
public class OrderShippingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private com.ecom.sage.common.entity.OrderStatus status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    public OrderShippingStatus(){
        super();
        this.createdAt= LocalDateTime.now();
    }
    public OrderShippingStatus(com.ecom.sage.common.entity.OrderStatus status){
        super();
        this.status = status;
        this.createdAt= LocalDateTime.now();
    }
}
