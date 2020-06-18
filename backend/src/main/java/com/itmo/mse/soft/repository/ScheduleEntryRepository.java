package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.schedule.ScheduleEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ScheduleEntryRepository extends CrudRepository<ScheduleEntry, UUID> {
    @Query("select se from ScheduleEntry as se " +
            "where (:to between se.timeStart and se.timeEnd or " +
            ":from between se.timeStart and se.timeEnd or " +
            "se.timeStart between :to and :from)")
    List<ScheduleEntry> findAllIntersecting(Instant from, Instant to);
}
