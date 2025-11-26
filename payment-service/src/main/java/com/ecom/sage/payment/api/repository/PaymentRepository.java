package com.ecom.sage.payment.api.repository;

import com.ecom.sage.payment.api.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, String> {
    PaymentTransaction findByOrderId(String orderId);
}
