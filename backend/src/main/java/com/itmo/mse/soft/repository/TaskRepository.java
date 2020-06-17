package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.task.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {
}
