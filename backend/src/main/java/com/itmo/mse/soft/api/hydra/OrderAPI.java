package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.repository.OrderRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderAPI {

  final Queue<BodyOrder> bodyOrderQueue = new ConcurrentLinkedDeque<>();
  OrderRepository orderRepository;

  public List<BodyOrder> receiveNewOrders() {
    if (bodyOrderQueue.isEmpty()) {
      return Collections.emptyList();
    }
    List<BodyOrder> ret = new ArrayList<>(bodyOrderQueue);
    bodyOrderQueue.clear();
    return ret;

  }

  @SneakyThrows
  public synchronized void queueOrderAndWaitResults(BodyOrder bodyOrder) {
    bodyOrderQueue.offer(bodyOrder);
    while (!bodyOrder.isCancelled() && !bodyOrder.isConfirmed()) {
      wait();
    }
  }

  public synchronized boolean confirmOrder(BodyOrder bodyOrder, String bitcoinAddress, String bodyStateUrl) {
    notifyAll();
    log.info("Order '{}' is confirmed: '{}', '{}'", bodyOrder.getOrderId(), bitcoinAddress, bodyStateUrl);
    return true;
  }

  public synchronized void cancelOrder(UUID orderId) {
    notifyAll();
    log.info("Order '{}' is canceled. No free slots.", orderId);
  }

  public void setTimeSlotInfo(List<TimeSlotInfo> timeSlotInfos) {
    log.info("Time slot info set: '{}'", timeSlotInfos);
  }
}
