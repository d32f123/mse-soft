package com.itmo.mse.soft.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.id.UUIDGenerationStrategy;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.id.uuid.StandardRandomStrategy;

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

    @Column
    @Type(type="uuid-char")
    private UUID paymentId;

    @Column
    private String barcode;

    @Column
    @Enumerated(EnumType.STRING)
    private BodyState state;

}
