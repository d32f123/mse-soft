package com.itmo.mse.soft.entity;

import java.math.BigInteger;
import javax.persistence.*;

@Entity
public class Body {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected BigInteger id;
    protected BigInteger paymentId;
    protected String barCode;
    protected String state;
}
