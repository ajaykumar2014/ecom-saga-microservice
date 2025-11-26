package com.ecom.sage.payment.api.service;

import com.ecom.sage.common.entity.OrderStatus;
import com.ecom.sage.common.events.*;
import com.ecom.sage.payment.api.entity.PaymentTransaction;
import com.ecom.sage.payment.api.entity.model.PaymentStatus;
import com.ecom.sage.payment.api.repository.PaymentRepository;
import com.google.common.flogger.FluentLogger;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentProcessorService {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    @Value("${spring.kafka.topic.inventoryPaymentEventChannel}")
    private String inventoryPaymentEventChannel;

    @Value("${spring.kafka.topic.paymentShippingEventChannel}")
    private String paymentShippingEventChannel;

    private final PaymentRepository repository;
    private final KafkaTemplate<String, RequestEvent> kafkaTemplate;
    private BigDecimal balanceAmt = new BigDecimal("10000.00");

    public PaymentProcessorService(PaymentRepository repository, KafkaTemplate<String, RequestEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public PaymentTransaction processPayment(PaymentRequestedEvent event) {
        LOGGER.atInfo().log("✅ Processing payment for order %s", event);
        PaymentTransaction paymentTransaction = null;
        int available = event.getOutstandingAmt().compareTo(balanceAmt);
        if (available > 0) {
            publishPaymentFailedEvent(event);
        } else {
            balanceAmt = balanceAmt.subtract(event.getOutstandingAmt());
            String transactionId = UUID.randomUUID().toString();
            PaymentTransaction payment = new PaymentTransaction();
            payment.setId(transactionId);
            payment.setOrderId(event.getOrderId());
            payment.setOutstandingAmt(event.getOutstandingAmt());
            payment.setPaidAmt(event.getOutstandingAmt());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreated_date(LocalDateTime.now());
            paymentTransaction = repository.save(payment);
            PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent(
                    event.getOrderId(), event.getProductId(),
                    event.getOutstandingAmt(), payment.getPaidAmt(), transactionId, true, "PAYMENT_SUCCESS"
            );
            LOGGER.atInfo().log("✅ Deducting Balance %s and total balance is %s for order %s", payment.getPaidAmt(),balanceAmt,event.getOrderId());
            try {
                kafkaTemplate.send(paymentShippingEventChannel, event.getOrderId(), paymentSuccessEvent).get();
                LOGGER.atInfo().log("✅ Published event PaymentSuccessEvent successful %s", paymentSuccessEvent);
            } catch (Exception ex) {
                LOGGER.atSevere().log("❌Published event PaymentSuccessEvent unsuccessful to topic={%s} key={%s} error={%s}",
                        paymentShippingEventChannel, event.getOrderId(), ex);
                throw new RuntimeException("Failed to send event to topic: " + ex);
            }
        }
        return paymentTransaction;
    }

    @Transactional
    public PaymentTransaction revokePayment(ShippingSuccessEvent shippingSuccessEvent) {
        LOGGER.atInfo().log("✅ Revoking payment for order %s", shippingSuccessEvent);
        PaymentTransaction payment = repository.findById(shippingSuccessEvent.getTransactionId()).orElseThrow(() -> new RuntimeException("Transaction Id Not found"));
        payment.setStatus(PaymentStatus.FAILED);
        repository.save(payment);
        balanceAmt = balanceAmt.add(shippingSuccessEvent.getPaidAmt());
        LOGGER.atInfo().log("✅ Revoking Balance %s and total balance is %s for orderId %s", shippingSuccessEvent.getPaidAmt(),balanceAmt,shippingSuccessEvent.getOrderId());
        PaymentRevokeEvent paymentRevoke = new PaymentRevokeEvent(
                payment.getOrderId(), shippingSuccessEvent.getProductId(),
                payment.getOutstandingAmt(), payment.getPaidAmt(), payment.getId(), "PAYMENT_REVOKE"
        );
        try {
            kafkaTemplate.send(inventoryPaymentEventChannel, payment.getOrderId(), paymentRevoke).get();
            LOGGER.atInfo().log("✅ Published event PaymentRevokeEvent successful %s", paymentRevoke);
        } catch (Exception ex) {
            LOGGER.atSevere().log("❌Published event PaymentRevokeEvent unsuccessful to topic={%s} key={%s} error={%s}",
                    inventoryPaymentEventChannel, payment.getOrderId(), ex);
            throw new RuntimeException("Failed to send event to topic: " + ex);
        }
        return payment;
    }

    public void publishPaymentFailedEvent(PaymentRequestedEvent paymentRequestedEvent) {
        String orderId = paymentRequestedEvent.getOrderId();
        PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(orderId, paymentRequestedEvent.getProductId(), "INSUFFICIENT_BALANCE");
        try {
            kafkaTemplate.send(inventoryPaymentEventChannel, orderId, paymentFailedEvent);
            LOGGER.atInfo().log("✅ Requested Order Id %s,  PaymentFailedEvent published, ", orderId);
        } catch (Exception ex) {
            // In case of publish failure, log; retry will attempt again due to @Retry
            LOGGER.atInfo().log("❌ Requested Order Id %s,  Exception while publishing PaymentFailedEvent %s ", orderId, ex);
            throw new RuntimeException(ex);
        }
    }

    public void publishPaymentSuccessEvent(PaymentTransaction paymentTransaction, String productId) {
        String orderId = paymentTransaction.getOrderId();
        PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent(orderId, productId, paymentTransaction.getOutstandingAmt(), paymentTransaction.getPaidAmt(), paymentTransaction.getId(), true, "PAYMENT_SUCCESS");
        try {
            kafkaTemplate.send(inventoryPaymentEventChannel, orderId, paymentSuccessEvent);
            LOGGER.atInfo().log("✅ Requested Order Id %s,  PaymentSuccessEvent published, ", orderId);
        } catch (Exception ex) {
            // In case of publish failure, log; retry will attempt again due to @Retry
            LOGGER.atInfo().log("❌ Requested Order Id %s,  Exception while publishing PaymentSuccessEvent , ", orderId);
            throw new RuntimeException(ex);
        }
    }

    public void publishShippingReadyEvent(ShippingSuccessEvent shippingSucceededEvent) {
        String orderId = shippingSucceededEvent.getOrderId();
        try {
            kafkaTemplate.send(inventoryPaymentEventChannel, shippingSucceededEvent.getOrderId(), shippingSucceededEvent);
            LOGGER.atInfo().log("✅ Requested Order Id %s,  ShippingSucceededEvent published, ", orderId);
        } catch (Exception ex) {
            // In case of publish failure, log; retry will attempt again due to @Retry
            LOGGER.atInfo().log("❌ Requested Order Id %s,  Exception while publishing ShippingSucceededEvent , ", orderId);
            throw new RuntimeException(ex);
        }
    }

    public void publishShippingCancelledEvent(ShippingSuccessEvent shippingSucceededEvent) {
        String orderId = shippingSucceededEvent.getOrderId();
        OrderStatus orderStatus = shippingSucceededEvent.getStatus();
        try {
            kafkaTemplate.send(inventoryPaymentEventChannel, orderId, shippingSucceededEvent);
            LOGGER.atInfo().log("✅ Requested Order Id %s,  ShippingSuccessEvent[%s] published, ", orderId,orderStatus.name());
        } catch (Exception ex) {
            // In case of publish failure, log; retry will attempt again due to @Retry
            LOGGER.atInfo().log("❌ Requested Order Id %s,  Exception while publishing ShippingSuccessEvent[%s] %s ", orderId,orderStatus.name(), ex);
            throw new RuntimeException(ex);
        }
    }

}

