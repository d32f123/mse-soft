package com.itmo.mse.soft.task;

public enum TaskType {

    PICKUP(false), GROOM(false), FEED(true), REGULAR_FEED(true);

    public final boolean isFeedingTask;

    TaskType(boolean isFeedingTask) {
        this.isFeedingTask = isFeedingTask;
    }
}
