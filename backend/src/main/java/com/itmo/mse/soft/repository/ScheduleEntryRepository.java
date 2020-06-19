package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.schedule.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {
    @Query("select case when count(se) > 0 then true else false end from ScheduleEntry as se " +
            "where (:to between se.timeStart and se.timeEnd or " +
            ":from between se.timeStart and se.timeEnd or " +
            "se.timeStart between :from and :to)")
    boolean existAnyIntersecting(Instant from, Instant to);

    @Query("select se from ScheduleEntry as se " +
            "where (:to between se.timeStart and se.timeEnd or " +
            ":from between se.timeStart and se.timeEnd or " +
            "se.timeStart between :from and :to)")
    List<ScheduleEntry> findAllIntersecting(Instant from, Instant to);
}
