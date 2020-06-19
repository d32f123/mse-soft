package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class OrderAPI {
    Queue<Order> orderQueue = new LinkedList<>();

    public OrderAPI() {
    }

    public List<Order> receiveNewOrders() {
        if (orderQueue.isEmpty()) {
            return Collections.emptyList();
        }
        var ret = List.copyOf(orderQueue);
        orderQueue.clear();
        return ret;
    }

    public void confirmOrder(UUID orderId, String bitcoinAddress, String bodyStateUrl) {
        log.info("Order '{}' is confirmed: '{}', '{}'", orderId, bitcoinAddress, bodyStateUrl);
    }

    public void cancelOrder(UUID orderId) {
        log.info("Order '{}' is canceled", orderId);
    }

    public void setTimeSlotInfo(List<TimeSlotInfo> timeSlotInfos) {
        log.info("Time slot info set: '{}'", timeSlotInfos);
    }
}
