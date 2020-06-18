package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.*;
import com.itmo.mse.soft.order.Order;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.*;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.task.SubTask;
import com.itmo.mse.soft.task.SubTaskType;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
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
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void savesAndLoadsBody() {
        var order = Order.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .paymentInstant(Instant.now())
                .build();

        var payment = Payment.builder()
                .bitcoinAddress("someaddress")
                .creationInstant(Instant.now())
                .order(order)
                .build();


        Body body = Body.builder().state(BodyState.IN_FEEDING).barcode("somecode").payment(payment).build();

        bodyRepository.save(body);

        body.getPayment().setBitcoinAddress("someotheraddress");
        bodyRepository.save(body);

        Body savedBody = bodyRepository.findById(body.getId()).orElseThrow();
        assertThat(savedBody).isEqualToIgnoringGivenFields(body, "payment");
        assertThat(savedBody.getPayment().getPaymentId()).isEqualTo(body.getPayment().getPaymentId());
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

    @Test
    void savesAndLoadsOrder() {
        Order order = Order.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .paymentInstant(Instant.now())
                .build();

        orderRepository.save(order);
        var loadedOrder = orderRepository.findById(order.getOrderId()).orElseThrow();

        assertThat(loadedOrder).isEqualToIgnoringGivenFields(order, "paymentAmount");
        assertThat(loadedOrder.getPaymentAmount().equals(order.getPaymentAmount()));
    }

    @Test
    void savesAndLoadsPayment() {
        Order order = Order.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .paymentInstant(Instant.now())
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .creationInstant(Instant.now())
                .bitcoinAddress("somebitcoin")
                .build();

        paymentRepository.save(payment);
        var loadedPayment = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(loadedPayment).isEqualToIgnoringGivenFields(payment, "order");
        assertThat(loadedPayment.getOrder()).isNotNull();
        assertThat(loadedPayment.getOrder()).isEqualToIgnoringGivenFields(order, "paymentAmount");
    }

    @Test
    void savesAndLoadsPigsty() {
        var pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(3)
                .build();
        pigstyRepository.save(pigsty);

        var loadedPigsty = pigstyRepository.findById(pigsty.getPigstyId()).orElseThrow();
        assertThat(loadedPigsty).isEqualToComparingFieldByField(pigsty);
    }

    @Test
    void savesAndLoadsTask() {
        var employee = Employee.builder()
                .name("Vasya")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        employeeRepository.save(employee);

        var scheduleEntry = ScheduleEntry.builder()
                .timeStart(Instant.now())
                .timeEnd(Instant.now())
                .subEntries(IntStream.range(0, 2).mapToObj(i -> ScheduleEntry.builder()
                    .timeStart(Instant.now())
                    .timeEnd(Instant.now())
                    .build()).collect(Collectors.toList())
                ).build();
        scheduleEntry.getSubEntries().forEach(subEntry -> subEntry.setParent(scheduleEntry));

        Body body = Body.builder()
                .payment(Payment.builder()
                        .creationInstant(Instant.now())
                        .bitcoinAddress("asdf")
                        .order(Order.builder()
                            .paymentInstant(Instant.now())
                            .paymentAmount(new BigDecimal("135.50"))
                            .build())
                        .build())
                .state(BodyState.IN_GROOMING)
                .barcode("somecoolcode")
                .build();
        bodyRepository.save(body);

        var pigsty = Pigsty.builder()
                .pigAmount(6)
                .pigstyNumber(0)
                .build();
        pigstyRepository.save(pigsty);


        Task task = Task.builder()
                .taskType(TaskType.GROOM)
                .body(body)
                .employee(employee)
                .scheduleEntry(scheduleEntry)
                .isComplete(false)
                .pigsty(pigsty)
                .build();

        task.setSubTasks(
                IntStream.range(0, 2)
                        .mapToObj(
                                i -> SubTask.builder()
                                .parent(task)
                                .isComplete(true)
                                .scheduleEntry(scheduleEntry.getSubEntries().get(i))
                                .subTaskType(SubTaskType.PICKUP_FROM_CUSTOMER)
                                .build()
                        ).collect(Collectors.toList())
        );
        taskRepository.save(task);

        var loadedTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        assertThat(loadedTask).isEqualToIgnoringGivenFields(task,
                "subTasks", "scheduleEntry", "subTasks");
        assertThat(loadedTask.getSubTasks().isEmpty()).isFalse();
        assertThat(loadedTask.getSubTasks().size()).isEqualTo(task.getSubTasks().size());
        assertThat(loadedTask.getScheduleEntry().getScheduleEntryId())
                .isEqualByComparingTo(task.getScheduleEntry().getScheduleEntryId());
        assertThat(loadedTask.getScheduleEntry().getSubEntries()).isNotNull();
        assertThat(loadedTask.getScheduleEntry().getSubEntries().isEmpty()).isFalse();
        assertThat(loadedTask.getScheduleEntry().getSubEntries().size())
                .isEqualTo(scheduleEntry.getSubEntries().size());
    }

    @Test
    void savesAndLoadsSubTask() {
        var scheduleEntry = ScheduleEntry.builder()
                .timeStart(Instant.now())
                .timeEnd(Instant.now())
                .subEntries(
                        Collections.singletonList(ScheduleEntry.builder()
                                .timeStart(Instant.now())
                                .timeEnd(Instant.now())
                                .build())
                )
                .build();
        scheduleEntry.getSubEntries().get(0).setParent(scheduleEntry);

        var employee = Employee.builder()
                .name("Vasya")
                .employeeRole(EmployeeRole.GROOMER)
                .build();

        employeeRepository.save(employee);

        Task task = Task.builder()
                .employee(employee)
                .scheduleEntry(scheduleEntry)
                .isComplete(false)
                .taskType(TaskType.GROOM)
                .build();

        taskRepository.save(task);

        SubTask subTask = SubTask.builder()
                .subTaskType(SubTaskType.SHAVE)
                .isComplete(false)
                .scheduleEntry(scheduleEntry.getSubEntries().get(0))
                .parent(task)
                .build();
        subTaskRepository.save(subTask);

        var loadedSubTask = subTaskRepository.findById(subTask.getSubTaskId()).orElseThrow();
        assertThat(loadedSubTask).isEqualToIgnoringGivenFields(subTask,
                "parent", "scheduleEntry");
        assertThat(loadedSubTask.getScheduleEntry()).isEqualToIgnoringGivenFields(
                scheduleEntry.getSubEntries().get(0), "parent", "subEntries");
        assertThat(loadedSubTask.getParent()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isEqualByComparingTo(task.getTaskId());
        assertThat(loadedSubTask.getScheduleEntry().getSubEntries()).isEmpty();
        assertThat(loadedSubTask.getParent()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isEqualByComparingTo(task.getTaskId());

        assertThat(loadedSubTask.getScheduleEntry().getParent()).isNotNull();
    }
}
