package com.ecom.sage.shipping.api.repo;

import com.ecom.sage.shipping.api.entity.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<ShippingOrder, String> {
    Optional<ShippingOrder> findByOrderId(String orderId);
}
