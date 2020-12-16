package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.OrderRepository;
import com.itmo.mse.soft.repository.PaymentRepository;
import com.itmo.mse.soft.service.BodyService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hydra/orders")
@Slf4j
@Transactional
public class OrderStubController {

    @Autowired
    private OrderAPI orderAPI;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BodyService bodyService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public boolean submitOrder(@RequestBody BodyOrder bodyOrder) {
        bodyOrder.setOrderId(UUID.randomUUID());
        if (bodyOrder.getPickupInstant() == null)
            bodyOrder.setPickupInstant(Instant.now());
        return this.orderAPI.bodyOrderQueue.offer(bodyOrder);
    }

    @PutMapping
    public ResponseEntity payForOrder(@RequestBody ExecutePayment executePayment) {
        Payment payment = paymentRepository.getOne(executePayment.paymentId);
        BodyOrder order = orderRepository.findByPaymentId(executePayment.paymentId).orElseThrow();
        if (new BigDecimal(0).compareTo(executePayment.amount) > 0)
            return ResponseEntity.badRequest().body("Bad amount");
        if (order.getPaymentAmount().compareTo(executePayment.amount) > 0)
            order.setPaymentAmount(order.getPaymentAmount().subtract(executePayment.amount));
        else if (order.getPaymentAmount().compareTo(executePayment.amount) == 0)
            order.setPaymentAmount(new BigDecimal(0));
        else if (order.getPaymentAmount().compareTo(executePayment.amount) < 0)
            return ResponseEntity.badRequest().body("Too much amount");
        orderRepository.save(order);
        return ResponseEntity.ok(order);
    }
}
