package com.itmo.mse.soft.manager;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.task.TaskManager;
import com.itmo.mse.soft.task.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@ActiveProfiles("postgres")
public class TaskManagerTests {

    @Autowired
    TaskManager taskManager;
    @Autowired
    TestHelper testHelper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    BodyRepository bodyRepository;

    @Test
    void contextLoads() {}

    @Test
    void shouldFindDailyTasks() {
        var employee = Employee.builder()
                .name("someguy")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        var body = testHelper.createBody(BodyState.AWAITING_RECEIVAL);
        bodyRepository.save(body);

        employeeRepository.save(employee);

        var instance = testHelper.getTimeAt(12, 20);
        var task = taskManager.scheduleTaskAt(instance, TaskType.PICKUP, body);
        assertThat(task).isNotNull();

        var anotherTask = taskManager.scheduleTask(TaskType.GROOM, body, instance);

        assertThat(taskManager.getDailyTasks(employee.getEmployeeId(), LocalDate.ofInstant(instance, ZoneOffset.UTC)));
    }
}
