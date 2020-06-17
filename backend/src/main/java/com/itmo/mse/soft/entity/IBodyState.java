package com.itmo.mse.soft.entity;

public interface IBodyState {

    public BodyState getBodyState();
    public boolean isInFridge();

    public enum BodyState {
        AWAITING_RECEIVAL,
        IN_RECEIVAL,
        RECEIVED,
        IN_GROOMING,
        GROOMED,
        IN_FEEDING,
        FED
    }
}
