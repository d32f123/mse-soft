package com.itmo.mse.soft.schedule;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ScheduleEntry {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID scheduleEntryId;

    @Column(nullable = false)
    private Instant timeStart;

    @Column(nullable = false)
    private Instant timeEnd;

}
