package com.itmo.mse.soft.task;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Pigsty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public interface TaskManager {
    boolean scheduleBody(Instant pickupInstant, Body body);

    Task scheduleFeedingTaskAt(Instant Start, Pigsty pigsty);
    Task scheduleFeedingTask(Pigsty pigsty);
    Task scheduleFeedingTask(Pigsty pigsty, Instant searchFrom);

    List<Task> getDailyTasks(UUID employeeId);
    List<Task> getDailyTasks(UUID employeeId, LocalDate day);

    Task completeTask(UUID taskId, UUID employeeId);
    Task completeSubTask(UUID subTaskId, UUID employeeId);
}
