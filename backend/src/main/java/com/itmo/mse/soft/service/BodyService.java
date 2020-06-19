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

@Slf4j
@Service
public class BodyService extends EntityService<Body> {

    @Autowired
    protected BodyRepository entityRepository;

    @Override
    protected JpaRepository<Body, UUID> getEntityRepository() {
        return entityRepository;
    }

    @Autowired
    private TaskManager taskManager;

    public Body createBody(Payment payment) {
        var body = Body.builder()
                .payment(payment)
                .state(BodyState.AWAITING_RECEIVAL)
                .barcode(issueBarcode())
                .build();

        this.entityRepository.save(body);

        if (taskManager.scheduleBody(payment.getOrder().getPickupInstant(), body)) {
            return body;
        }

        // Uncommit
        this.entityRepository.delete(body);
        return null;
    }

    public Optional<Body> getBodyByBarcode(String barcode) {
        return entityRepository.findBodyByBarcode(barcode);
    }

    public Optional<Body> getBodyByPaymentId(UUID paymentId) {
        return entityRepository.findBodyByPayment(Payment.builder().paymentId(paymentId).build());
    }

    public String issueBarcode() {
        log.debug("Issuing barcode");

        return UUID.randomUUID().toString();
    }
}
