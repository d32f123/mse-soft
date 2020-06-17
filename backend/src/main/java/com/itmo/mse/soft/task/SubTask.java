package com.itmo.mse.soft.task;

import com.itmo.mse.soft.schedule.ScheduleEntry;
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
public class SubTask {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID subTaskId;

    @ManyToOne
    private Task parent;

    @Column
    private boolean isComplete;

    @OneToOne
    private ScheduleEntry scheduleEntry;

    @Column
    @Enumerated(EnumType.STRING)
    private SubTaskType subTaskType;

}
