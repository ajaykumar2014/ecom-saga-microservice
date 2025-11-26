package com.ecom.sage.payment.api.listener;


import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.common.events.PaymentEvent;
import com.ecom.sage.common.events.PaymentRequestedEvent;
import com.ecom.sage.common.events.ShippingSuccessEvent;
import com.ecom.sage.payment.api.entity.PaymentTransaction;
import com.ecom.sage.payment.api.service.PaymentProcessorService;
import com.google.common.flogger.FluentLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentRequestListener {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
    private final PaymentProcessorService service;

    public PaymentRequestListener(PaymentProcessorService paymentProcessorService) {
        this.service = paymentProcessorService;
    }

    @RetryableTopic(
            attempts = "3",
            autoCreateTopics = "false",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlq",
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${spring.kafka.topic.inventoryPaymentEventChannel}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentRequest(ConsumerRecord<String, PaymentEvent> consumerRecord) {
        PaymentEvent event = consumerRecord.value();
        if (event instanceof PaymentRequestedEvent e) {
            LOGGER.atInfo().log("🟢 PaymentRequestedEvent Received for orderId - %s", event);
            PaymentTransaction paymentTransaction = service.processPayment(e);
            if (paymentTransaction != null) {
                service.publishPaymentSuccessEvent(paymentTransaction, e.getProductId());
            }
        }
    }

    @RetryableTopic(
            attempts = "3",
            autoCreateTopics = "false",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlq",
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${spring.kafka.topic.paymentShippingEventChannel}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeShippingEventRequest(ConsumerRecord<String, PaymentEvent> consumerRecord) {
        PaymentEvent event = consumerRecord.value();
        if (event instanceof ShippingSuccessEvent e) {
            if (e.getStatus() == OrderStatus.SHIPPED_READY || e.getStatus() == OrderStatus.SHIPPED) {
                LOGGER.atInfo().log("🟢 ShippingSuccessEvent[SHIPPED_READY/SHIPPED] Received for orderId - %s", event);
                service.publishShippingReadyEvent(e);
            }
            if (e.getStatus() == OrderStatus.SHIPPED_CANCELLED || e.getStatus() == OrderStatus.SHIPMENT_FAILED) {
                LOGGER.atInfo().log("🟢 ShippingSuccessEvent[SHIPPED_CANCELLED/SHIPMENT_FAILED] Received for orderId - %s", event);
                PaymentTransaction paymentTransaction = service.revokePayment(e);
                if (paymentTransaction != null) {
                    service.publishShippingCancelledEvent(e);
                }
            }

        }
    }
}

