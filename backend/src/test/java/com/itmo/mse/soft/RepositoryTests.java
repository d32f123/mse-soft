package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.repository.*;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RepositoryTests {

    @Autowired
    private BodyRepository bodyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PigstyRepository pigstyRepository;
    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;
    @Autowired
    private SubTaskRepository subTaskRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Test
    void savesBody() {
        Body body = Body.builder().state(BodyState.IN_GROOMING).barcode("somecode").build();

        bodyRepository.save(body);
        assertThat(body.getId()).isNotNull();
    }

    @Test
    void loadsBody() {
        Body body = Body.builder().state(BodyState.IN_FEEDING).barcode("somecode").paymentId(UUID.randomUUID()).build();

        bodyRepository.save(body);

        Body savedBody = bodyRepository.findById(body.getId()).orElseThrow();
        assertThat(savedBody).isEqualToComparingFieldByField(body);
    }

    @Test
    void savesAndLoadsEmployee() {
        Employee employee = Employee.builder().name("Shureek").employeeRole(EmployeeRole.GROOMER).build();

        employeeRepository.save(employee);
        var savedBody = employeeRepository.findById(employee.getEmployeeId()).orElseThrow();

        assertThat(savedBody).isEqualToComparingFieldByField(employee);
    }

    @Test
    void savesAndLoadsScheduleEntry() {
        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .timeStart(Instant.now())
                .timeEnd(Instant.now()).build();

        scheduleEntry.setSubEntries(IntStream.range(0, 2).mapToObj(i -> ScheduleEntry.builder()
                .parent(scheduleEntry)
                .timeStart(Instant.now())
                .timeEnd(Instant.now())
                .build()).collect(Collectors.toList()));

        scheduleEntryRepository.save(scheduleEntry);

        var loadedEntry = scheduleEntryRepository.findById(scheduleEntry.getScheduleEntryId()).orElseThrow();
        assertThat(loadedEntry).isEqualToIgnoringGivenFields(scheduleEntry, "subEntries");
        assertThat(loadedEntry.getSubEntries().size()).isEqualTo(scheduleEntry.getSubEntries().size());
    }
}
