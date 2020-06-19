package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.order.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BodyRepository extends JpaRepository<Body, UUID> {

    Optional<Body> findBodyByBarcode(String barcode);
    Optional<Body> findBodyByPayment(Payment payment);

}
