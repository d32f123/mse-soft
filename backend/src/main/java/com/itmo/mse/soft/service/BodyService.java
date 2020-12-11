package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class BodyService extends EntityService<Body> {

    @Autowired
    protected BodyRepository bodyRepository;

    @Override
    protected JpaRepository<Body, UUID> getEntityRepository() {
        return bodyRepository;
    }

    @Autowired
    private TaskManager taskManager;

    public Body createBody(Payment payment) {
        var body = Body.builder()
                .payment(payment)
                .state(BodyState.AWAITING_RECEIVAL)
                .barcode(null)
                .build();

        this.bodyRepository.save(body);

        if (taskManager.scheduleBody(payment.getBodyOrder().getPickupInstant(), body)) {
            return body;
        }

        // Uncommit
        this.bodyRepository.delete(body);
        return null;
    }

    public Optional<Body> getBodyByBarcode(String barcode) {
        return bodyRepository.findBodyByBarcode(barcode);
    }

    public Optional<Body> getBodyByPaymentId(UUID paymentId) {
        return bodyRepository.findBodyByPayment(Payment.builder().paymentId(paymentId).build());
    }

}
