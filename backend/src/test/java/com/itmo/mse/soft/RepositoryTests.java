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
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TestHelper testHelper;

    Body createBody() {
        return Body.builder()
                .payment(
                        Payment.builder()
                                .order(
                                        Order.builder()
                                        .pickupInstant(Instant.now())
                                        .paymentAmount(new BigDecimal("556.00")).build()
                                ).creationInstant(Instant.now())
                                .bitcoinAddress("asdjkasdjkasdkj").build()
                ).state(BodyState.RECEIVED)
                .barcode(UUID.randomUUID().toString())
                .build();
    }

    @Test
    void savesAndLoadsBody() {
        var order = Order.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .pickupInstant(Instant.now())
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

        scheduleEntryRepository.save(scheduleEntry);

        var loadedEntry = scheduleEntryRepository.findById(scheduleEntry.getScheduleEntryId()).orElseThrow();
        assertThat(loadedEntry).isEqualToIgnoringGivenFields(scheduleEntry, "subEntries");
    }

    @Test
    void savesAndLoadsOrder() {
        Order order = Order.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .pickupInstant(Instant.now())
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
                .pickupInstant(Instant.now())
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
                .timeEnd(Instant.now()).build();

        Body body = Body.builder()
                .payment(Payment.builder()
                        .creationInstant(Instant.now())
                        .bitcoinAddress("asdf")
                        .order(Order.builder()
                            .pickupInstant(Instant.now())
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
                                .scheduleEntry(ScheduleEntry.builder()
                                        .timeStart(Instant.now())
                                        .timeEnd(Instant.now()).build())
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
    }

    @Test
    void savesAndLoadsSubTask() {
        var scheduleEntry = ScheduleEntry.builder()
                .timeStart(Instant.now())
                .timeEnd(Instant.now())
                .build();

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
                .scheduleEntry(ScheduleEntry.builder()
                        .timeStart(Instant.now())
                        .timeEnd(Instant.now())
                        .build())
                .parent(task)
                .build();
        subTaskRepository.save(subTask);

        var loadedSubTask = subTaskRepository.findById(subTask.getSubTaskId()).orElseThrow();
        assertThat(loadedSubTask).isEqualToIgnoringGivenFields(subTask,
                "parent", "scheduleEntry");
        assertThat(loadedSubTask.getParent()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isEqualByComparingTo(task.getTaskId());
        assertThat(loadedSubTask.getParent()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isNotNull();
        assertThat(loadedSubTask.getParent().getTaskId()).isEqualByComparingTo(task.getTaskId());
    }

    @Test
    void shouldFindTaskInBetween() {
        var pigsty = Pigsty.builder()
                .pigstyNumber(123)
                .pigAmount(30).build();
        pigstyRepository.save(pigsty);

        var employee = Employee.builder()
                .name("vasya")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();
        employeeRepository.save(employee);

        var entry = ScheduleEntry.builder()
                .timeStart(testHelper.getDayAt(3, 12, 2020))
                .timeEnd(testHelper.getDayAt(6, 12, 2020))
                .build();

        var body = createBody();
        bodyRepository.save(body);

        var task = Task.builder()
                .taskType(TaskType.FEED)
                .isComplete(false)
                .employee(employee)
                .pigsty(pigsty)
                .body(body)
                .scheduleEntry(entry).build();

        taskRepository.save(task);

        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(4, 12, 2020),
                testHelper.getDayAt(7, 12, 2020),
                pigsty.getPigstyId()
        )).isTrue();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(4, 12, 2020),
                testHelper.getDayAt(7, 12, 2020)
        )).isNotEmpty();
        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(2, 12, 2020),
                testHelper.getDayAt(4, 12, 2020),
                pigsty.getPigstyId()
        )).isTrue();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(2, 12, 2020),
                testHelper.getDayAt(4, 12, 2020)
        )).isNotEmpty();
        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(1, 12, 2020),
                testHelper.getDayAt(2, 12, 2020),
                pigsty.getPigstyId()
        )).isFalse();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(1, 12, 2020),
                testHelper.getDayAt(2, 12, 2020)
        )).isEmpty();
        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(7, 12, 2020),
                testHelper.getDayAt(8, 12, 2020),
                pigsty.getPigstyId()
        )).isFalse();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(7, 12, 2020),
                testHelper.getDayAt(8, 12, 2020)
        )).isEmpty();
        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(4, 12, 2020),
                testHelper.getDayAt(5, 12, 2020),
                pigsty.getPigstyId()
        )).isTrue();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(4, 12, 2020),
                testHelper.getDayAt(5, 12, 2020)
        )).isNotEmpty();
        assertThat(taskRepository.existsByPigstyAndTimeIntersection(
                testHelper.getDayAt(2, 12, 2020),
                testHelper.getDayAt(8, 12, 2020),
                pigsty.getPigstyId()
        )).isTrue();
        assertThat(taskRepository.findIntersectionsByEmployeeRoleAndTime(
                EmployeeRole.PIG_MASTER,
                testHelper.getDayAt(4, 12, 2020),
                testHelper.getDayAt(5, 12, 2020)
        )).isNotEmpty();
    }
}
