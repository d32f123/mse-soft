package com.itmo.mse.soft.entity;

import java.time.Instant;
import java.util.Calendar;
import java.util.List;

public class Pigsty {
    static int pigAmount;
    static Instant lastFeedScheduleEntry = Instant.now();

    public static void feedPigs(Body body){
        body.setBodyState(IBodyState.BodyState.IN_FEEDING);
        lastFeedScheduleEntry = Instant.now();
        body.setBodyState(IBodyState.BodyState.FED);
    }

}
