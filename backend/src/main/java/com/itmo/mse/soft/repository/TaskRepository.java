package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Task> findByTaskId(UUID id);

    List<Task> findAllByIsComplete(boolean complete);

    @Query("select case when count(taskId) > 0 then true else false end from Task " +
            "where pigsty.pigstyId = :pigstyId and " +
            "(:to between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            ":from between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            "scheduleEntry.timeStart between :from and :to)")
    boolean existsByPigstyAndTimeIntersection(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("pigstyId") UUID pigstyId);

    @Query("from Task where employee.employeeRole = :employeeRole and " +
            "(:to between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            ":from between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            "scheduleEntry.timeStart between :from and :to)")
    List<Task> findIntersectionsByEmployeeRoleAndTime(EmployeeRole employeeRole, Instant from, Instant to);

    @Query("select case when count(taskId) > 0 then true else false end " +
            "from Task where employee.employeeRole = :employeeRole and " +
            "(:to between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            ":from between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            "scheduleEntry.timeStart between :from and :to)")
    boolean existIntersectionsByEmployeeRoleAndTime(EmployeeRole employeeRole, Instant from, Instant to);

    @Query("from Task where employee.employeeId = :employeeId and " +
            "(:to between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            ":from between scheduleEntry.timeStart and scheduleEntry.timeEnd or " +
            "scheduleEntry.timeStart between :from and :to) " +
            "order by scheduleEntry.timeStart")
    List<Task> findIntersectionsByEmployeeIdAndTime(UUID employeeId, Instant from, Instant to);
}
