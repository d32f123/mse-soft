package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.order.Payment;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PaymentRepository extends CrudRepository<Payment, UUID> {
}
