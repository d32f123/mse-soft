package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.task.SubTask;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SubTaskRepository extends CrudRepository<SubTask, UUID> {
}
