package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PigstyService extends EntityService<Pigsty> {

    @Autowired
    private PigstyRepository pigstyRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Override
    protected CrudRepository<Pigsty, UUID> getEntityRepository() {
        return pigstyRepository;
    }

    public Task reservePigsty(UUID pigstyId, UUID taskId) {
        var pigsty = pigstyRepository.findById(pigstyId).orElseThrow();
        var task = taskRepository.findById(taskId).orElseThrow();
        return reservePigsty(pigsty, task);
    }

    public Task reservePigsty(Pigsty pigsty, Task task) {
        if (task.getTaskType() != TaskType.FEED && task.getTaskType() != TaskType.REGULAR_FEED) {
            log.warn("Pigsty '{}' is trying to be registered for task '{}', but it is not a feeding one",
                    pigsty.getPigstyId(), task.getTaskId());
            return task;
        }
        if (taskRepository.existsByPigstyAndTimeIntersection(
                task.getScheduleEntry().getTimeStart(),
                task.getScheduleEntry().getTimeEnd(),
                pigsty.getPigstyId()
        )) {
            log.info("Pigsty '{}' is already reserved at some task during '{}'--'{}'",
                    pigsty.getPigstyId(),
                    task.getScheduleEntry().getTimeStart(),
                    task.getScheduleEntry().getTimeEnd());
            return task;
        }

        task.setPigsty(pigsty);
        return taskRepository.save(task);
    }

    public Pigsty feedPigsty(Task task) {
        assert task.getPigsty() != null;

        task.getPigsty().setLastFedTask(task);
        return pigstyRepository.save(task.getPigsty());
    }

}
