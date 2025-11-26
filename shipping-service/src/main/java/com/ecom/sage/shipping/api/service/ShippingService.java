package com.ecom.sage.shipping.api.service;

import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.common.events.PaymentSuccessEvent;
import com.ecom.sage.common.events.RequestEvent;
import com.ecom.sage.common.events.ShippingSuccessEvent;
import com.ecom.sage.shipping.api.entity.OrderShippingStatus;
import com.ecom.sage.shipping.api.entity.ShippingOrder;
import com.ecom.sage.shipping.api.repo.ShippingRepository;
import com.google.common.flogger.FluentLogger;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShippingService {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    @Value("${spring.kafka.topic.paymentShippingEventChannel}")
    private String paymentShippingEventChannel;

    private final ShippingRepository repository;
    private final KafkaTemplate<String, RequestEvent> kafkaTemplate;


    public ShippingService(ShippingRepository repository, KafkaTemplate<String, RequestEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void doOrderShipping(PaymentSuccessEvent paymentEvent) {
        LOGGER.atInfo().log("✅ Received PaymentSuccessEvent %s", paymentEvent);
        String shippingId = UUID.randomUUID().toString();
        ShippingOrder shippingOrder = new ShippingOrder();
        shippingOrder.setId(shippingId);
        shippingOrder.setOrderId(paymentEvent.getOrderId());
        shippingOrder.setProductId(paymentEvent.getProductId());
        shippingOrder.setOutstandingAmt(paymentEvent.getOutstandingAmt());
        shippingOrder.setTotalAmt(paymentEvent.getPaidAmt());
        shippingOrder.setCreatedAt(LocalDateTime.now());
        shippingOrder.setTransactionId(paymentEvent.getTransactionId());
        shippingOrder.getOrderShippingHistory().add(new OrderShippingStatus(OrderStatus.valueOf(OrderStatus.SHIPPED_READY.name())));
        shippingOrder.setStatus(OrderStatus.valueOf(OrderStatus.SHIPPED_READY.name()));
        LOGGER.atInfo().log("✅ Before Saving PaymentSuccessEvent %s", shippingOrder);
        repository.save(shippingOrder);
        LOGGER.atInfo().log("✅ After Saving to ShippingDB with %s", shippingOrder);
        // publish OrderCreated
        ShippingSuccessEvent evt = new ShippingSuccessEvent(shippingId, paymentEvent.getOrderId(),
                paymentEvent.getProductId(),
                paymentEvent.getTransactionId(),
                paymentEvent.getPaidAmt(),
                paymentEvent.getOutstandingAmt(),
                OrderStatus.valueOf(OrderStatus.SHIPPED_READY.name()),
                shippingOrder.getCreatedAt(),"SHIPPED_READY");
        kafkaTemplate.send(paymentShippingEventChannel, shippingId, evt).whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.atSevere().log("❌ Failed to send event to topic={%s} key={%s} error={%s}",
                        paymentShippingEventChannel, shippingId, ex.getMessage(), ex);
                return;
            }
            RecordMetadata m = result.getRecordMetadata();
            LOGGER.atInfo().log("✅ Sent to topic={%s} partition={%s} offset={%s} key={%s} value={%s}",
                    m.topic(), m.partition(), m.offset(), shippingId, evt);
        });
    }

    @Transactional
    public ShippingOrder doUpdateShippingStatus(String shippingId, String status) {
        LOGGER.atInfo().log("✅ Updating Shipping Status for shippingId -  %s and status ", shippingId, status);
        ShippingOrder shippingOrder = repository.findById(shippingId).orElseThrow(() -> new RuntimeException("Shipping id not found - " + shippingId));
        shippingOrder.setStatus(OrderStatus.valueOf(status));
        shippingOrder.getOrderShippingHistory().add(new OrderShippingStatus(OrderStatus.valueOf(status)));
        repository.save(shippingOrder);
        LOGGER.atInfo().log("✅ ShippingStatus Updated %s", shippingOrder);
        // publish OrderCreated
        ShippingSuccessEvent evt = new ShippingSuccessEvent(shippingId, shippingOrder.getOrderId(),
                shippingOrder.getProductId(),
                shippingOrder.getTransactionId(),
                shippingOrder.getTotalAmt(),
                shippingOrder.getOutstandingAmt(),
                OrderStatus.valueOf(status),
                shippingOrder.getCreatedAt(),status);
        try {
            kafkaTemplate.send(paymentShippingEventChannel, shippingId, evt).get();
            LOGGER.atInfo().log("✅ Published event ShippingSuccessEvent successful %s", evt);
        } catch (Exception ex) {
            LOGGER.atSevere().log("❌Published event ShippingSuccessEvent unsuccessful to topic={%s} key={%s} error={%s}",
                    paymentShippingEventChannel, shippingOrder.getOrderId(), ex);
            throw new RuntimeException("Failed to send event to topic: " + ex);
        }
        return shippingOrder;
    }
}
