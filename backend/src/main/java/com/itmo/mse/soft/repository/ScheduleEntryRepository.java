package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.schedule.ScheduleEntry;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ScheduleEntryRepository extends CrudRepository<ScheduleEntry, UUID> {
}
