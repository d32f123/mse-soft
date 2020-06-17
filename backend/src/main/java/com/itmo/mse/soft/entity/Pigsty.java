package com.itmo.mse.soft.entity;

import java.time.Instant;

public class Pigsty {
    static int pigAmount;
    static Instant lastFeedScheduleEntry = Instant.now();

    public static void feedPigs(Body body){
        body.setBodyState(BodyState.BodyState.IN_FEEDING);
        lastFeedScheduleEntry = Instant.now();
        body.setBodyState(BodyState.BodyState.FED);
    }

}
