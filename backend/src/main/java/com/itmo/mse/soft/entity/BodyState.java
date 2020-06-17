package com.itmo.mse.soft.entity;

public enum BodyState {

    AWAITING_RECEIVAL(false),
    IN_RECEIVAL(false),
    RECEIVED(true),
    IN_GROOMING(false),
    GROOMED(true),
    IN_FEEDING(false),
    FED(false);

    public final boolean isInFridge;

    private BodyState(boolean isInFridge) {
        this.isInFridge = isInFridge;
    }
}
