package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
