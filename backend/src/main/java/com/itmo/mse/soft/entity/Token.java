package com.itmo.mse.soft.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne(optional = false)
    private Employee employee;

    @Column(nullable = false)
    private Instant expirationInstant;

}
