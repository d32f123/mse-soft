package com.itmo.mse.soft.entity;

import com.itmo.mse.soft.order.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.UUID;
import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Body {

    @Id
    @Column
    @GeneratedValue
    @Type(type="uuid-char")
    private UUID id;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Payment payment;

    @Column
    private String barcode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BodyState state;

}
