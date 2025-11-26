package com.ecom.sage.order.api.service;

import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.common.events.OrderCreatedEvent;
import com.ecom.sage.common.events.RequestEvent;
import com.ecom.sage.order.api.client.ProductDetailsClient;
import com.ecom.sage.order.api.constant.Constant;
import com.ecom.sage.order.api.entity.Order;
import com.ecom.sage.order.api.entity.OrderStatusDetail;
import com.ecom.sage.order.api.exception.OrderIdNotFoundException;
import com.ecom.sage.order.api.model.CreateOrderRequest;
import com.ecom.sage.order.api.model.CreateOrderResponse;
import com.ecom.sage.order.api.model.Product;
import com.ecom.sage.order.api.repo.OrderRepository;
import com.ecom.sage.order.api.response.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.flogger.FluentLogger;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    @Value("${spring.kafka.topic.orderInventoryEventChannel}")
    private String orderInventoryEventChannel;

    private final OrderRepository repo;
    private final KafkaTemplate<String, RequestEvent> kafkaTemplate;
    private final ProductClientService productDetailsClient;

    public OrderService(OrderRepository repo, KafkaTemplate<String, RequestEvent> kafka, ProductClientService productDetailsClient) {
        this.repo = repo;
        this.kafkaTemplate = kafka;
        this.productDetailsClient = productDetailsClient;
    }

    @Transactional
    @RetryableTopic
    public Order createOrder(CreateOrderRequest req) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setId(orderId);
        order.setProductId(req.getProductId());
        order.setUserId(req.getCustomerId());
        order.setQuantity(req.getQuantity());
        order.setAmount(req.getPrice());
        order.setStatus(OrderStatus.valueOf(OrderStatus.PENDING.name()));
        order.setCreated_date(LocalDateTime.now());
        order.getOrderStatusDetails().add(new OrderStatusDetail(OrderStatus.valueOf(OrderStatus.PENDING.name()), ""));
        Order savedEntity = repo.save(order);
        LOGGER.atInfo().log("✅ Sent to OrderDB with %s", order);
        // publish OrderCreated
        try {
            OrderCreatedEvent evt = new OrderCreatedEvent(orderId, order.getUserId(), order.getProductId(), order.getQuantity(), req.getPrice(), order.getCreated_date(),"ORDER_CREATED");
            kafkaTemplate.send(orderInventoryEventChannel, orderId, evt).get();
            LOGGER.atInfo().log("✅ Published event OrderCreatedEvent successful %s", evt);
        } catch (Exception ex) {
            LOGGER.atSevere().log("❌ Failed to send event to topic={%s} key={%s} error={%s}",
                    orderInventoryEventChannel, orderId, ex);
            throw new RuntimeException("Failed to send event to topic: " + ex);
        }
        return savedEntity;
    }

    @Transactional
    public void doUpdateOrderRequest(RequestEvent payload, OrderStatus status, String shippingId, String transactionId, String reason) {
        String orderId = payload.getOrderId();
        repo.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            o.setShippingId(shippingId == null ? "" : shippingId);
            o.setTransactionId(transactionId == null ? "" : transactionId);
            o.getOrderStatusDetails().add(new OrderStatusDetail(status, reason));
            repo.save(o);
        });
    }

    public OrderResponse getOrderByOrderId(String orderId) {
        Order orderResponse =  repo.findById(orderId).orElseThrow(() -> new OrderIdNotFoundException("OrderId not found - " + orderId));

        Product product = productDetailsClient.callProductDetails(orderResponse.getProductId());
        product.setQuantity(orderResponse.getQuantity());

        return OrderResponse.builder()
                .orderId(orderResponse.getId())
                .orderStatusDetails(orderResponse.getOrderStatusDetails())
                .created_date(orderResponse.getCreated_date())
                .currentStatus(orderResponse.getStatus())
                .products(List.of(product))
                .totalAmount(new BigDecimal(orderResponse.getQuantity()).multiply(product.getPricePerUnit()))
                .shippingId(orderResponse.getShippingId())
                .transactionId(orderResponse.getTransactionId())
                .created_date(orderResponse.getCreated_date()).build();

    }
}
