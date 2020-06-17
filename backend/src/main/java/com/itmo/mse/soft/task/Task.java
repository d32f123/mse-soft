package com.itmo.mse.soft.task;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Task {

    @Id
    @Column
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID taskId;

    @ManyToOne
    private Employee employee;

    @OneToOne
    private ScheduleEntry scheduleEntry;

    @Column
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column
    private boolean isComplete;

    @ManyToOne
    private Body body;

    @ManyToOne
    private Pigsty pigsty;

    @OneToMany(mappedBy = "parent")
    private List<SubTask> subTasks;

}
