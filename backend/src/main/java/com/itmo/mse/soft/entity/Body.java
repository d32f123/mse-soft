package com.itmo.mse.soft.entity;

import java.math.BigInteger;
import javax.persistence.*;

@Entity
public class Body implements IBodyState {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected BigInteger id;
    protected BigInteger paymentId;
    protected String barCode;
    protected String state;
    protected BodyState bodyState;
    protected boolean isInFridge;

    public BodyState getBodyState() {
        return this.bodyState;
    }

    public boolean isInFridge() {
        return this.isInFridge;
    }
}
