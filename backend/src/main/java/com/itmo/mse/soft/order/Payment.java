package com.itmo.mse.soft.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID paymentId;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Order order;

    @Column(nullable = false)
    private String bitcoinAddress;

    @Column(nullable = false)
    private Instant creationInstant;

}
