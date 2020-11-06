package com.itmo.mse.soft.config;

import com.itmo.mse.soft.order.Order;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

public class OrderEventHandler extends AbstractRepositoryEventListener<Order> {

    @HandleBeforeSave
    public void onBeforeSave(Order entity) {
    //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }

    @HandleBeforeCreate
    public void onBeforeCreate(Order entity) {
        //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }

    @HandleAfterSave
    public void onAfterSave(Order entity) {
        //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }
}
