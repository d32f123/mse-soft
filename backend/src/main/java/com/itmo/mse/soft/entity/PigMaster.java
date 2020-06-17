package com.itmo.mse.soft.entity;

public class PigMaster  extends  Employee{

    public void doJob(Body body) {
        Pigsty.feedPigs(body);
    }
}
