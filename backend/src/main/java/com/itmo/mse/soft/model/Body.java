package com.itmo.mse.soft.model;

import java.math.BigInteger;
import javax.persistence.*;

public class Body {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected BigInteger id;
    protected BigInteger paymentId;
    protected String barCode;
    protected String state;
}
