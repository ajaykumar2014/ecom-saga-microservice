package com.ecom.sage.shipping.api.listener;


import com.ecom.sage.common.events.PaymentEvent;
import com.ecom.sage.common.events.PaymentSuccessEvent;
import com.ecom.sage.shipping.api.service.ShippingService;
import com.google.common.flogger.FluentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingOrderListener {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
    private final ShippingService service;

    @RetryableTopic(
            attempts = "3",
            autoCreateTopics = "false",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlq",
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${spring.kafka.topic.paymentShippingEventChannel}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentRequest(ConsumerRecord<String, PaymentEvent> event) {
        PaymentEvent paymentEvent = event.value();
        if (paymentEvent instanceof PaymentSuccessEvent e){
            LOGGER.atInfo().log("✅ Received PaymentSuccessEvent value - %s,  PaymentSuccessEvent published, ", e);
            service.doOrderShipping(e);
        }

    }
}

