package com.itmo.mse.soft.entity;

import com.itmo.mse.soft.service.ReaderService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Reader {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID readerId;

    @Column
    @Enumerated(EnumType.STRING)
    private ReaderLocation location;

}
