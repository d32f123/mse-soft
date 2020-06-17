package com.itmo.mse.soft.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID orderId;

    @Column(nullable = false)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private Instant paymentInstant;

}
