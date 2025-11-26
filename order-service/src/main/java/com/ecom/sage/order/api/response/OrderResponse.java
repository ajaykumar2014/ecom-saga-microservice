package com.ecom.sage.order.api.response;

import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.order.api.entity.OrderStatusDetail;
import com.ecom.sage.order.api.model.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder
public class OrderResponse implements Serializable {

    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("products")
    private List<Product> products;
    private String transactionId;
    private String shippingId;
    private OrderStatus currentStatus;
    private BigDecimal totalAmount;
    private List<OrderStatusDetail> orderStatusDetails;
    private LocalDateTime created_date;

}

