package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@Service
@Slf4j
public class OrderAPI {
    Queue<Order> orderQueue = new LinkedList<>();

    public OrderAPI() {
        this.orderQueue.add(Order.builder()
            .paymentAmount(new BigDecimal("5556.00"))
            .paymentInstant(Instant.now()).build());
        this.orderQueue.add(Order.builder()
        .paymentInstant(Instant.now().plus(Duration.ofMinutes(5)))
        .paymentAmount(new BigDecimal("3333.00")).build());
    }

    public List<Order> receiveNewOrders() {
        return List.of(Objects.requireNonNull(orderQueue.poll()));
    }

    public void confirmOrder(Order order, String bitcoinAddress, String bodyStateUrl) {
    }

    public void setTimeSlotInfo(List<TimeSlotInfo> timeSlotInfos) {
    }
}
