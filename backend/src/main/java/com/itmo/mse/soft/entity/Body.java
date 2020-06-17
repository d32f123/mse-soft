package com.itmo.mse.soft.entity;

import lombok.Data;

import java.util.UUID;
import javax.persistence.*;

@Data
public class Body {

    private UUID id;
    private UUID paymentId;
    private String barcode;
    private BodyState state;

}
