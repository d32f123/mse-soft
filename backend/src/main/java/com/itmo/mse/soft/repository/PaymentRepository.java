package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  List<Payment> findAllByBodyOrder(BodyOrder bodyOrder);
}
