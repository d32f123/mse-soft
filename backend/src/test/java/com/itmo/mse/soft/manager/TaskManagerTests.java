package com.itmo.mse.soft.manager;

import static org.assertj.core.api.Assertions.assertThat;

import com.itmo.mse.soft.TableCleaner;
import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskManager;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    @Autowired
    PigstyRepository pigstyRepository;
    @Autowired
    TableCleaner tableCleaner;

    @Test
    void contextLoads() {}

    @Test
    void shouldFindDailyTasks() {
        tableCleaner.clearTables();
        Employee employee = Employee.builder()
                .name("someguy")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        Employee anotherEmployee = Employee.builder()
                .name("groomer")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();
        Body body = testHelper.createBody(BodyState.AWAITING_RECEIVAL);
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(2)
                .build();
        bodyRepository.save(body);

        employeeRepository.save(employee);
        employeeRepository.save(anotherEmployee);

        pigstyRepository.save(pigsty);

        Instant instance = testHelper.getTimeAt(12, 20);
        boolean scheduled = taskManager.scheduleBody(instance, body);
        assertThat(scheduled).isTrue();


        assertThat(taskManager.getDailyTasks(
            employee.getEmployeeId(),
            LocalDate.of(instance.get(ChronoField.YEAR), instance.get(ChronoField.MONTH_OF_YEAR),
                instance.get(ChronoField.DAY_OF_MONTH))
        ).size()).isEqualTo(2);
    }

    @Test
    void shouldNotCreateTaskIfNoEmployeesAvailable() {
        tableCleaner.clearTables();
        Employee employee = Employee.builder()
                .name("someguy")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        Employee anotherEmployee = Employee.builder()
                .name("groomer")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();

        employeeRepository.save(employee);
        employeeRepository.save(anotherEmployee);
        Body body = testHelper.createBody(BodyState.AWAITING_RECEIVAL);
        bodyRepository.save(body);
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(2)
                .build();
        pigstyRepository.save(pigsty);

        Instant instance = testHelper.getTimeAt(12, 20);
        boolean scheduled = taskManager.scheduleBody(instance, body);
        assertThat(scheduled).isTrue();

        boolean anotherScheduled = taskManager.scheduleBody(instance.plus(Duration.ofMinutes(20)), body);
        assertThat(anotherScheduled).isFalse();
    }

    @Test
    void shouldReturnNullWhenNoEmployees() {
        tableCleaner.clearTables();
        Body body = testHelper.createBody(BodyState.GROOMED);
        assertThat(taskManager.scheduleBody(Instant.now(), body)).isFalse();
    }

    @Test
    void shouldNotReserveOnSameTime() {
        tableCleaner.clearTables();
        Employee employee = Employee.builder()
                .name("someguy")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        Employee anotherEmployee = Employee.builder()
                .name("groomer")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();

        employeeRepository.save(employee);
        employeeRepository.save(anotherEmployee);
        Body body = testHelper.createBody(BodyState.AWAITING_RECEIVAL);
        Body anotherBody = testHelper.createBody(BodyState.AWAITING_RECEIVAL);
        bodyRepository.save(body);
        bodyRepository.save(anotherBody);
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(2)
                .build();
        Pigsty anotherPigsty = Pigsty.builder()
                .pigstyNumber(1)
                .pigAmount(5)
                .build();
        pigstyRepository.save(pigsty);
        pigstyRepository.save(anotherPigsty);

        Instant pickupInstant = testHelper.getTimeAt(13, 50);
        taskManager.scheduleBody(pickupInstant, body);

        List<Task> firstTasks = taskManager.getDailyTasks(employee.getEmployeeId(), pickupInstant.atZone(ZoneOffset.UTC).toLocalDate());
        List<Task> pigMasterTasks = taskManager.getDailyTasks(anotherEmployee.getEmployeeId(), pickupInstant.atZone(ZoneOffset.UTC).toLocalDate());
        assertThat(firstTasks).hasSize(2);
        assertThat(firstTasks.get(0).getScheduleEntry().getTimeEnd()).isBeforeOrEqualTo(
                firstTasks.get(1).getScheduleEntry().getTimeStart()
        );

        assertThat(taskManager.scheduleBody(pickupInstant, body)).isFalse();

        assertThat(taskManager.scheduleBody(pickupInstant.minus(Duration.ofMinutes(75)), body)).isTrue();
        List<Task> newTasks = taskManager.getDailyTasks(employee.getEmployeeId(), pickupInstant.atZone(ZoneOffset.UTC).toLocalDate());
        List<Task> pigNewTasks = taskManager.getDailyTasks(anotherEmployee.getEmployeeId(), pickupInstant.atZone(ZoneOffset.UTC).toLocalDate());
        assertThat(newTasks).hasSize(4);
        assertThat(newTasks.get(0).getScheduleEntry().getTimeStart()).isEqualTo(pickupInstant.minus(Duration.ofMinutes(75)));
        assertThat(newTasks.get(3).getScheduleEntry().getTimeStart()).isAfterOrEqualTo(firstTasks.get(1).getScheduleEntry().getTimeEnd());

        // PigMaster assertions
        assertThat(pigMasterTasks).hasSize(1);
        assertThat(pigNewTasks).hasSize(2);
        assertThat(pigMasterTasks.get(0).getPigsty().getPigstyId()).isNotEqualByComparingTo(pigNewTasks.get(1).getPigsty().getPigstyId());
    }
}
