package com.ecom.sage.order.api.config;

import com.ecom.sage.order.api.constant.Constant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.ecom.sage.order.api.constant.Constant.*;

@Configuration
public class KafkaConfig {


    @Bean
    public NewTopic orderInventoryEventChannel() {
        return TopicBuilder.name("order-inventory-event-topic").partitions(1).replicas(3).build();
    }

    @Bean
    public NewTopic inventoryPaymentEventChannel() {
        return TopicBuilder.name("inventory-payment-event-topic").partitions(1).replicas(3).build();
    }

    @Bean
    public NewTopic paymentShippingEventChannel() {
        return TopicBuilder.name("payment-shipping-event-topic").partitions(1).replicas(3).build();
    }
    @Bean
    public NewTopic orderInventoryEventChannelDLQ() {
        return TopicBuilder.name("order-inventory-event-topic-dlq").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryPaymentEventChannelDLQ() {
        return TopicBuilder.name("inventory-payment-event-topic-dlq").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentShippingEventChannelDLQ() {
        return TopicBuilder.name("payment-shipping-event-topic-dlq").partitions(1).replicas(1).build();
    }
    @Bean
    public NewTopic orderInventoryEventChannelRetry() {
        return TopicBuilder.name("order-inventory-event-topic-retry").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryPaymentEventChannelRetry() {
        return TopicBuilder.name("inventory-payment-event-topic-retry").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentShippingEventChannelRetry() {
        return TopicBuilder.name("payment-shipping-event-topic-retry").partitions(1).replicas(1).build();
    }

}
