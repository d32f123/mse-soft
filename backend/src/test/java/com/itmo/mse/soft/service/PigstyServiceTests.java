package com.itmo.mse.soft.service;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PigstyServiceTests {

    @Autowired
    PigstyService pigstyService;

    @Autowired
    PigstyRepository pigstyRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    BodyRepository bodyRepository;

    @Autowired
    TestHelper testHelper;

    @Test
    void contextLoads() {}

    @Test
    void shouldReservePigsty() {
        var employee = testHelper.createEmployee(EmployeeRole.PIG_MASTER);
        var body = testHelper.createBody();
        employeeRepository.save(employee);
        bodyRepository.save(body);

        var scheduleEntry = ScheduleEntry.builder()
                .timeStart(testHelper.getTimeAt(10, 0))
                .timeEnd(testHelper.getTimeAt(12, 0))
                .build();
        var task = Task.builder()
                .scheduleEntry(scheduleEntry)
                .body(body)
                .employee(employee)
                .taskType(TaskType.FEED)
                .build();

        taskRepository.save(task);

        var pigsty = Pigsty.builder()
                .pigAmount(32)
                .pigstyNumber(39391)
                .build();

        pigstyRepository.save(pigsty);

        pigstyService.reservePigsty(pigsty, task);
        assertThat(task.getPigsty()).isEqualToComparingFieldByField(pigsty);

        var otherTask = Task.builder()
                .scheduleEntry(
                        ScheduleEntry.builder()
                        .timeStart(testHelper.getTimeAt(11, 30))
                        .timeEnd(testHelper.getTimeAt(13, 30))
                        .build()
                ).body(body)
                .employee(employee)
                .taskType(TaskType.FEED)
                .build();
        taskRepository.save(otherTask);

        pigstyService.reservePigsty(pigsty, otherTask);
        assertThat(otherTask.getPigsty()).isNull();
    }

}
