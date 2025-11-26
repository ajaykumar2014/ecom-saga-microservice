package com.ecom.sage.order.api.listener;

import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.common.events.*;
import com.ecom.sage.order.api.repo.OrderRepository;
import com.ecom.sage.order.api.service.OrderService;
import com.google.common.flogger.FluentLogger;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderInventoryChannelListener {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
    private final OrderRepository repo;
    private final OrderService orderService;

    @RetryableTopic(
            attempts = "3",
            autoCreateTopics = "false",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlq",
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${spring.kafka.topic.orderInventoryEventChannel}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onInventoryReserved(ConsumerRecord<String, RequestEvent> consumerRecord) {
        try {
            RequestEvent payload = consumerRecord.value();
            if (payload instanceof OrderCreatedEvent) {
                return;
            }
            if (payload instanceof InventoryReservedEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.INVENTORY_RESERVED, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 InventoryReservedEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof InventoryFailedEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.INVENTORY_FAILED, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 InventoryFailedEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof InventoryRollbackEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.INVENTORY_ROLLBACK, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 InventoryRollbackEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof PaymentSuccessEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.PAYMENT_COMPLETED, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 PaymentSuccessEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof PaymentPendingEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.PAYMENT_PENDING, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 PaymentPendingEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof PaymentRevokeEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.PAYMENT_REVERTED, "","",e.getNotes());
                LOGGER.atInfo().log("🟢 PaymentRevertEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof PaymentFailedEvent e) {
                orderService.doUpdateOrderRequest(payload, OrderStatus.PAYMENT_FAILED,"","", e.getNotes());
                LOGGER.atInfo().log("🚫 PaymentFailedEvent Received for orderId - %s", payload.getOrderId());
            }
            if (payload instanceof ShippingSuccessEvent e) {
                orderService.doUpdateOrderRequest(payload, e.getStatus(), e.getShippingId(),e.getTransactionId(), e.getNotes());
                LOGGER.atInfo().log("🟢 ShippingSucceededEvent[%s] Received for orderId - %s", e.getStatus(), payload.getOrderId());
            }

        } catch (Exception ex) {
            LOGGER.atSevere().log("❌Exception raised while listener the event [%s] and reason [%s]", consumerRecord.value(), ex);
        }
    }



}

