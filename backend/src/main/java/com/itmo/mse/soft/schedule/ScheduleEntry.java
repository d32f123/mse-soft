package com.itmo.mse.soft.schedule;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"parent"})
public class ScheduleEntry{

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID scheduleEntryId;

    @Column(nullable = false)
    private Instant timeStart;

    @Column(nullable = false)
    private Instant timeEnd;

    @ManyToOne(cascade = CascadeType.MERGE)
    private ScheduleEntry parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ScheduleEntry> subEntries;

}
