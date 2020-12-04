package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
public class OrderAPI {
    Queue<BodyOrder> bodyOrderQueue = new ConcurrentLinkedDeque<>();
    OrderRepository orderRepository;

    public OrderAPI() {
    }

    public List<BodyOrder> receiveNewOrders() {
        if (bodyOrderQueue.isEmpty()) {
            return Collections.emptyList();
        }
        var ret = List.copyOf(bodyOrderQueue);
        bodyOrderQueue.clear();
        return ret;

    }

    public void queueOrder(BodyOrder bodyOrder){
        bodyOrderQueue.add(bodyOrder);
    }

    public boolean confirmOrder(UUID orderId, String bitcoinAddress, String bodyStateUrl) {

        log.info("Order '{}' is confirmed: '{}', '{}'", orderId, bitcoinAddress, bodyStateUrl);
        return true;
    }

    public void cancelOrder(UUID orderId) {
        log.info("Order '{}' is canceled. No free slots.", orderId);
    }

    public void setTimeSlotInfo(List<TimeSlotInfo> timeSlotInfos) {
        log.info("Time slot info set: '{}'", timeSlotInfos);
    }
}
