package com.itmo.mse.soft.schedule;

import com.itmo.mse.soft.repository.ScheduleEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ScheduleManager {

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    public boolean existIntersections(Instant from, Instant to) {
        return scheduleEntryRepository.existAnyIntersecting(from, to);
    }

    public List<ScheduleEntry> getIntersections(Instant from, Instant to) {
        return scheduleEntryRepository.findAllIntersecting(from, to);
    }

    public ScheduleEntry registerEntry(ScheduleEntry scheduleEntry) {
        return scheduleEntryRepository.save(scheduleEntry);
    }
}
