package com.itmo.mse.soft.order;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.repository.OrderRepository;
import com.itmo.mse.soft.repository.PaymentRepository;
import com.itmo.mse.soft.service.BodyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderManager {

    @Autowired
    private OrderAPI orderAPI;
    @Autowired
    private BodyService bodyService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;



    public final Queue<BodyOrder> bodyOrderQueue = new ConcurrentLinkedQueue<>();

    @Scheduled(fixedDelay = 100)
    @Transactional
    public void orderConsumer() {
        while (!bodyOrderQueue.isEmpty()) {
            var order = bodyOrderQueue.poll();
            var orderId = order.getOrderId();
            order.setOrderId(null);
            var payment = generatePayment(order);

            var body = bodyService.createBody(payment);
            if (body == null) {
                order.setCancelled(true);
                orderAPI.cancelOrder(orderId);
                continue;
            }
            order.setPaymentId(body.getPayment().getPaymentId());
            order.setOrderId(body.getPayment().getBodyOrder().getOrderId());
//            paymentRepository.save(payment);
//            orderRepository.save(order);
//            bodyService.save(body);
            orderAPI.confirmOrder(order.getOrderId(), payment.getBitcoinAddress(), buildStateUrl(payment));
            order.setConfirmed(true);

        }
    }

    private Payment generatePayment(BodyOrder bodyOrder) {
        return Payment.builder()
                .creationInstant(Instant.now())
                .bitcoinAddress(generateBitcoinAddress())
                .bodyOrder(bodyOrder)
                .build();
    }

    @Scheduled(fixedRate = 100)
    public void orderProducer() {
        bodyOrderQueue.addAll(this.orderAPI.receiveNewOrders());
    }

    private String generateBitcoinAddress() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    private String buildStateUrl(Payment payment) {
        return "/order-status?paymentId=" + payment.getPaymentId().toString();
    }

}
