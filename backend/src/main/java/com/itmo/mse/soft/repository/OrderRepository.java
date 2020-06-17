package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.order.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrderRepository extends CrudRepository<Order, UUID> {
}
