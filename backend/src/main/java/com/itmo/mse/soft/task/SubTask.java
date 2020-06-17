package com.itmo.mse.soft.task;

import com.itmo.mse.soft.schedule.ScheduleEntry;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "parent")
public class SubTask {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID subTaskId;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private Task parent;

    @Column
    private boolean isComplete;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ScheduleEntry scheduleEntry;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubTaskType subTaskType;

}
