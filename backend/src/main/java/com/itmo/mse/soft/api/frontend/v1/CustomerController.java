package com.itmo.mse.soft.api.frontend.v1;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.service.BodyService;

import java.math.BigDecimal;
import java.time.Instant;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/order-status")
public class CustomerController {

    @Autowired
    BodyService bodyService;
    @Autowired
    OrderAPI orderAPI;

    @GetMapping
    public ResponseEntity<BodyState> getBodyState(@RequestParam("paymentId") UUID paymentId) {
        var body = bodyService.getBodyByPaymentId(paymentId).orElse(null);
        if (body == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(body.getState());
    }


    @PostMapping
    public ResponseEntity<BodyOrder> create() throws InterruptedException {
        BodyOrder createdBodyOrder = BodyOrder.builder()
                .orderId(UUID.randomUUID())
                .paymentAmount(new BigDecimal("123.50"))
                .pickupInstant(Instant.now())
                .build();
        orderAPI.queueOrder(createdBodyOrder);
        while (!createdBodyOrder.isCancelled() && !createdBodyOrder.isConfirmed()){
            Thread.sleep(100);
        }
        if (createdBodyOrder.isConfirmed())
            return ResponseEntity.ok(createdBodyOrder);
        else if (createdBodyOrder.isCancelled())
            return ResponseEntity.status(503).body(createdBodyOrder);
        throw new RuntimeException("Unexpected status");
    }
}
