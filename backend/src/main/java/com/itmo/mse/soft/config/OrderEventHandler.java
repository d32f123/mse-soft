package com.itmo.mse.soft.config;

import com.itmo.mse.soft.order.BodyOrder;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;

import java.time.temporal.ChronoUnit;

public class OrderEventHandler extends AbstractRepositoryEventListener<BodyOrder> {

    @HandleBeforeSave
    public void onBeforeSave(BodyOrder entity) {
    //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }

    @HandleBeforeCreate
    public void onBeforeCreate(BodyOrder entity) {
        //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }

    @HandleAfterSave
    public void onAfterSave(BodyOrder entity) {
        //... logic to handle inspecting the entity before the Repository saves it
        entity.setPickupInstant(entity.getPickupInstant().truncatedTo(ChronoUnit.MICROS));
    }
}
