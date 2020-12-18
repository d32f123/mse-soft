package com.itmo.mse.soft.api.frontend.v1;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.service.BodyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-status")
public class CustomerController {

    @Autowired
    BodyService bodyService;
    @Autowired
    OrderAPI orderAPI;

    @GetMapping
    public ResponseEntity getBodyState(@RequestParam("paymentId") UUID paymentId) {

        if (paymentId != null) {
            Body body = bodyService.getBodyByPaymentId(paymentId).orElse(null);
            if (body == null) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("rejected");
            }
            return ResponseEntity.ok(body.getState());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("rejected");
    }


    @PostMapping
    public ResponseEntity<?> create() throws InterruptedException {
        BodyOrder createdBodyOrder = BodyOrder.builder()
            .orderId(UUID.randomUUID())
            .paymentAmount(new BigDecimal("123.50"))
            .pickupInstant(Instant.now())
            .build();
        orderAPI.queueOrderAndWaitResults(createdBodyOrder);
        if (createdBodyOrder.isConfirmed()) {
            return ResponseEntity.ok(createdBodyOrder);
        } else if (createdBodyOrder.isCancelled()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("rejected");
        }
        throw new RuntimeException("Unexpected status");
    }
}
