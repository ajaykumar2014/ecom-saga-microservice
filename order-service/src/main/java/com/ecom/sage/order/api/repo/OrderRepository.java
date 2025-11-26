package com.ecom.sage.order.api.repo;

import com.ecom.sage.order.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {}
