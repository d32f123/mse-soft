package com.itmo.mse.soft.api.hydra;

import com.itmo.mse.soft.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hydra/orders")
@Slf4j
public class OrderStubController {

    @Autowired
    private OrderAPI orderAPI;

    @PostMapping
    public boolean submitOrder(@RequestBody Order order) {
        order.setOrderId(UUID.randomUUID());
        return this.orderAPI.orderQueue.offer(order);
    }

}
