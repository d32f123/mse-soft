package com.itmo.mse.soft.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Employee {

    @Id
    @Column
    @GeneratedValue
    @Type(type="uuid-char")
    private UUID employeeId;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;
}
