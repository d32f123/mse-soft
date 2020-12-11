package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.order.BodyOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<BodyOrder, UUID> {
  Optional<BodyOrder> findByPaymentId(UUID paymentId);
}
