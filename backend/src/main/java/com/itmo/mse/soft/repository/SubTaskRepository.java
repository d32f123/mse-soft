package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.task.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubTaskRepository extends JpaRepository<SubTask, UUID> {
}
