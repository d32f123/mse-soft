package com.itmo.mse.soft.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.OrderRepository;
import com.itmo.mse.soft.repository.PaymentRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ScheduleEntryRepository;
import com.itmo.mse.soft.repository.SubTaskRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.task.SubTask;
import com.itmo.mse.soft.task.SubTaskType;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
                                .bodyOrder(
                                        BodyOrder.builder()
                                        .pickupInstant(getInstant())
                                        .paymentAmount(new BigDecimal("556.00")).build()
                                ).creationInstant(getInstant())
                                .bitcoinAddress("asdjkasdjkasdkj").build()
                ).state(BodyState.RECEIVED)
                .barcode(UUID.randomUUID().toString())
                .build();
    }

    Instant getInstant(){
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    @Test
    void savesAndLoadsBody() {
        BodyOrder order = BodyOrder.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .pickupInstant(getInstant())
                .build();

        Payment payment = Payment.builder()
                .bitcoinAddress("someaddress")
                .creationInstant(getInstant())
                .bodyOrder(order)
                .build();


        Body body = Body.builder().state(BodyState.IN_FEEDING).barcode("somecode").payment(payment).build();

        bodyRepository.save(body);

        body.getPayment().setBitcoinAddress("someotheraddress");
        bodyRepository.save(body);
        Body savedBody = bodyRepository.findById(body.getId()).orElseThrow(() -> new RuntimeException());
        assertThat(savedBody).isEqualToIgnoringGivenFields(body, "payment");
        assertThat(savedBody.getPayment().getPaymentId()).isEqualTo(body.getPayment().getPaymentId());
    }

    @Test
    void savesAndLoadsEmployee() {
        Employee employee = Employee.builder().name("Shureek").employeeRole(EmployeeRole.GROOMER).build();

        employeeRepository.save(employee);
        Employee savedBody = employeeRepository.findById(employee.getEmployeeId())
            .orElseThrow(() -> new RuntimeException());

        assertThat(savedBody).isEqualToComparingFieldByField(employee);
    }

    @Test
    void savesAndLoadsScheduleEntry() {
        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .timeStart(getInstant())
                .timeEnd(getInstant()).build();

        scheduleEntryRepository.save(scheduleEntry);

        ScheduleEntry loadedEntry = scheduleEntryRepository.findById(scheduleEntry.getScheduleEntryId())
            .orElseThrow(() -> new RuntimeException());
        assertThat(loadedEntry).isEqualToIgnoringGivenFields(scheduleEntry, "subEntries");
    }

    @Test
    void savesAndLoadsOrder() {
        BodyOrder bodyOrder = BodyOrder.builder()
                .paymentAmount(new BigDecimal("123.50"))
                .pickupInstant(getInstant())
                .build();

        BodyOrder order2 = orderRepository.save(bodyOrder);
        BodyOrder loadedOrder = orderRepository.findById(bodyOrder.getOrderId())
            .orElseThrow(() -> new RuntimeException());

        assertThat(loadedOrder).isEqualToIgnoringGivenFields(bodyOrder, "paymentAmount");
        assertThat(loadedOrder.getPaymentAmount()).isEqualTo(bodyOrder.getPaymentAmount());
    }

    @Test
    void savesAndLoadsPayment() {
        BodyOrder bodyOrder = BodyOrder.builder()
                .paymentAmount(new BigDecimal("123.5"))
                .pickupInstant(getInstant())
                .build();

        Payment payment = Payment.builder()
                .bodyOrder(bodyOrder)
                .creationInstant(getInstant())
                .bitcoinAddress("somebitcoin")
                .build();

        paymentRepository.save(payment);
        Payment loadedPayment = paymentRepository.findById(payment.getPaymentId())
            .orElseThrow(() -> new RuntimeException());
        assertThat(loadedPayment).isEqualToIgnoringGivenFields(payment, "order");
        assertThat(loadedPayment.getBodyOrder()).isNotNull();
        assertThat(loadedPayment.getBodyOrder()).isEqualToIgnoringGivenFields(bodyOrder, "paymentAmount");
    }

    @Test
    void savesAndLoadsPigsty() {
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(3)
                .build();
        pigstyRepository.save(pigsty);

        Pigsty loadedPigsty = pigstyRepository.findById(pigsty.getPigstyId()).orElseThrow(() -> new RuntimeException());
        assertThat(loadedPigsty).isEqualToComparingFieldByField(pigsty);
    }

    @Test
    void savesAndLoadsTask() {
        Employee employee = Employee.builder()
                .name("Vasya")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        employeeRepository.save(employee);

        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .timeStart(getInstant())
                .timeEnd(getInstant()).build();

        Body body = Body.builder()
                .payment(Payment.builder()
                        .creationInstant(getInstant())
                        .bitcoinAddress("asdf")
                        .bodyOrder(BodyOrder.builder()
                            .pickupInstant(getInstant())
                            .paymentAmount(new BigDecimal("135.50"))
                            .build())
                        .build())
                .state(BodyState.IN_GROOMING)
                .barcode("somecoolcode")
                .build();
        bodyRepository.save(body);

        Pigsty pigsty = Pigsty.builder()
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
                                        .timeStart(getInstant())
                                        .timeEnd(getInstant()).build())
                                .subTaskType(SubTaskType.PICKUP_FROM_CUSTOMER)
                                .build()
                        ).collect(Collectors.toList())
        );
        taskRepository.save(task);

        Task loadedTask = taskRepository.findById(task.getTaskId()).orElseThrow(() -> new RuntimeException());
        assertThat(loadedTask).isEqualToIgnoringGivenFields(task,
                "subTasks", "scheduleEntry", "subTasks");
        assertThat(loadedTask.getSubTasks().isEmpty()).isFalse();
        assertThat(loadedTask.getSubTasks().size()).isEqualTo(task.getSubTasks().size());
        assertThat(loadedTask.getScheduleEntry().getScheduleEntryId())
                .isEqualByComparingTo(task.getScheduleEntry().getScheduleEntryId());
    }

    @Test
    void savesAndLoadsSubTask() {
        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .timeStart(getInstant())
                .timeEnd(getInstant())
                .build();

        Employee employee = Employee.builder()
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
                        .timeStart(getInstant())
                        .timeEnd(getInstant())
                        .build())
                .parent(task)
                .build();
        subTaskRepository.save(subTask);

        SubTask loadedSubTask = subTaskRepository.findById(subTask.getSubTaskId())
            .orElseThrow(() -> new RuntimeException());
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
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(123)
                .pigAmount(30).build();
        pigstyRepository.save(pigsty);

        Employee employee = Employee.builder()
                .name("vasya")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();
        employeeRepository.save(employee);

        ScheduleEntry entry = ScheduleEntry.builder()
                .timeStart(testHelper.getDayAt(3, 12, 2020))
                .timeEnd(testHelper.getDayAt(6, 12, 2020))
                .build();

        Body body = createBody();
        bodyRepository.save(body);

        Task task = Task.builder()
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
