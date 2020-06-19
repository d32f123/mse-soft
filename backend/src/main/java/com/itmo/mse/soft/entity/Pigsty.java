package com.itmo.mse.soft.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.itmo.mse.soft.task.Task;
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
public class Pigsty {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID pigstyId;

    @Column(nullable = false)
    int pigAmount;

    @Column(nullable = false)
    int pigstyNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JsonIgnore
    Task lastFedTask;

}
