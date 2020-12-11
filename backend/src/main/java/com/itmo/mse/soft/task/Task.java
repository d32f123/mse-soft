package com.itmo.mse.soft.task;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private Employee employee;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ScheduleEntry scheduleEntry;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column
    private boolean isComplete;

    @ManyToOne(cascade = CascadeType.MERGE)
    private Body body;

    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = CascadeType.MERGE)
    private Pigsty pigsty;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SubTask> subTasks;

}
