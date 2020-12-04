package com.itmo.mse.soft.order;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.repository.OrderRepository;
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



    public final Queue<BodyOrder> bodyOrderQueue = new ConcurrentLinkedQueue<>();

    @Scheduled(fixedDelay = 1000)
    public void orderConsumer() {
        while (!bodyOrderQueue.isEmpty()) {
            var order = bodyOrderQueue.poll();
            var orderId = order.getOrderId();
            order.setOrderId(null);
            var payment = generatePayment(order);

            var body = bodyService.createBody(payment);
            if (body == null) {
                orderAPI.cancelOrder(orderId);
                continue;
            }

            orderAPI.confirmOrder(orderId, payment.getBitcoinAddress(), buildStateUrl(payment));

        }
    }

    private Payment generatePayment(BodyOrder bodyOrder) {
        return Payment.builder()
                .creationInstant(Instant.now())
                .bitcoinAddress(generateBitcoinAddress())
                .bodyOrder(bodyOrder)
                .build();
    }

    @Scheduled(fixedRate = 1000)
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
