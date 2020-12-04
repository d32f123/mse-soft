package com.itmo.mse.soft;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.OrderManager;
import com.itmo.mse.soft.repository.*;
import com.itmo.mse.soft.service.AuthService;
import com.itmo.mse.soft.service.BodyService;
import com.itmo.mse.soft.service.EmployeeService;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.itmo.mse.soft.entity.BodyState.GROOMED;
import static com.itmo.mse.soft.entity.BodyState.RECEIVED;
import static com.itmo.mse.soft.task.SubTaskType.PICKUP_FROM_CUSTOMER;
import static com.itmo.mse.soft.task.SubTaskType.PRINT_BARCODE;
import static com.itmo.mse.soft.task.SubTaskType.PUT_IN_FRIDGE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusinessProcessTest {
  @Autowired
  private PlatformTransactionManager transactionManager;
  private TransactionTemplate transactionTemplate;
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
  @Autowired
  AuthService authService;
  @Autowired
  EmployeeService employeeService;
  @Autowired
  BodyService bodyService;
  @Autowired
  OrderManager orderManager;
  @Autowired
  OrderAPI orderAPI;

  private String barCode;
  //reserveSlot
  BodyOrder createdBodyOrder = BodyOrder.builder()
      .paymentAmount(new BigDecimal("123.50"))
      .pickupInstant(Instant.now())
      .build();


  @Test
  //2 //@Order(1)
  @SneakyThrows
  void canReserveSlot() {
    reserveSlot();
  }

  @Test
    //2 //@Order(2)
  void canLoginGroomer() {
    loginGroomer();
  }

  @Test
    //2 //@Order(3)
  void canAcceptBody() {
    acceptBody();

  }

  @Test
    //2 //@Order(4)
  void canPrintBarCode() {
    printBarCode();
  }

  @Test
    //2 //@Order(5)
  void canPutBodyInFreeze() {
    putBodyInFreeze();
  }

  @Test
    //2 //@Order(6)
  void canGroomBody() {
    groomBody();
  }

  @Test
    //2 //@Order(7)
  void canLoginPigMaster() {
    loginPigMaster();
  }

  @Test
    //2 //@Order(8)
  void canFeedPigs() {
    feedPigs();
  }


  @Test
    //2 //@Order(9)
  void canCheckBodyStatus() {
    checkBodyStatus();
  }


  @SneakyThrows
  void reserveSlot() {
    orderAPI.queueOrder(createdBodyOrder);
    //wait end of processing
    Thread.sleep(2000);
    var payments = paymentRepository.findAllByBodyOrder(createdBodyOrder);
    var body = bodyRepository.findBodyByPayment(payments.get(0)).orElseThrow();
    assertThat(createdBodyOrder.getOrderId()).isEqualTo(body.getPayment().getBodyOrder().getOrderId());
  }

  void loginGroomer() {
    reserveSlot(); //1
    var employee = employeeRepository.findAllByEmployeeRole(EmployeeRole.GROOMER).get(0);

    assertThat(authService.authenticate(employee.getName())).isBase64();
  }


  void acceptBody() {
    loginGroomer(); //1
    var employees = employeeService.getEmployeesByRole(EmployeeRole.GROOMER);
    var taskList = new ArrayList<Task>();
    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.PICKUP.equals(
                    t.getTaskType())).collect(Collectors.toList())));
    assertThat(taskList).hasSize(1);

    var pickUpTask = taskList.get(0);
    var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
        .filter(subTask -> PICKUP_FROM_CUSTOMER.equals(subTask.getSubTaskType())).collect(Collectors.toList());
    assertThat(acceptBodyTaskList).hasSize(1);

    var acceptBodyTask = acceptBodyTaskList.get(0);
    employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
    acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    assert acceptBodyTask != null;
    assertThat(acceptBodyTask.isComplete()).isTrue();
  }


  void printBarCode() {
    acceptBody(); //1
    var employees = employeeService.getEmployeesByRole(EmployeeRole.GROOMER);
    var taskList = new ArrayList<Task>();
    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.PICKUP.equals(
                    t.getTaskType())).collect(Collectors.toList())));
    assertThat(taskList).hasSize(1);

    var pickUpTask = taskList.get(0);
    var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
        .filter(subTask -> PRINT_BARCODE.equals(subTask.getSubTaskType())).collect(Collectors.toList());
    assertThat(acceptBodyTaskList).hasSize(1);

    var acceptBodyTask = acceptBodyTaskList.get(0);
    employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
    acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    assert acceptBodyTask != null;
    assertThat(acceptBodyTask.isComplete()).isTrue();

    var payments = paymentRepository.findAllByBodyOrder(createdBodyOrder);
    var body = bodyRepository.findBodyByPayment(payments.get(0));
    body.ifPresent(value -> barCode = value.getBarcode());
    assertThat(barCode).isNotEmpty();
  }


  void putBodyInFreeze() {
    printBarCode(); //1
    var employees = employeeService.getEmployeesByRole(EmployeeRole.GROOMER);
    var taskList = new ArrayList<Task>();

    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.PICKUP.equals(
                    t.getTaskType())).collect(Collectors.toList())));
    assertThat(taskList).hasSize(1);

    var pickUpTask = taskList.get(0);
    var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
        .filter(subTask -> PUT_IN_FRIDGE.equals(subTask.getSubTaskType())).collect(Collectors.toList());
    assertThat(acceptBodyTaskList).hasSize(1);

    var acceptBodyTask = acceptBodyTaskList.get(0);
    employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
    acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    assert acceptBodyTask != null;
    assertThat(acceptBodyTask.isComplete()).isTrue();
    assertThat(taskRepository.findAllByIsComplete(false)).hasSize(2);
    assertThat(bodyRepository.findBodyByBarcode(barCode).orElseThrow().getState()).isEqualTo(RECEIVED);
  }


  void groomBody() {
    putBodyInFreeze(); //1
    var employees = employeeService.getEmployeesByRole(EmployeeRole.GROOMER);
    var taskList = new ArrayList<Task>();

    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.GROOM.equals(
                    t.getTaskType())).collect(Collectors.toList())));
    assertThat(taskList).hasSize(1);

    var groomTask = taskList.get(0);
    for (var sub : groomTask.getSubTasks()){
      employeeService.completeSubTask(sub.getSubTaskId(), sub.getParent().getEmployee());
    }
    assertThat(taskRepository.findAllByIsComplete(false)).hasSize(1);
    assertThat(bodyRepository.findBodyByBarcode(barCode).orElseThrow().getState()).isEqualTo(GROOMED);
  }


  void loginPigMaster() {
    groomBody(); //1
    var employee = employeeRepository.findAllByEmployeeRole(EmployeeRole.PIG_MASTER).get(0);
    assertThat(authService.authenticate(employee.getName())).isBase64();
  }


  void feedPigs() {
    loginPigMaster(); //1
    var employees = employeeService.getEmployeesByRole(EmployeeRole.PIG_MASTER);
    var taskList = new ArrayList<Task>();

    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.FEED.equals(
                    t.getTaskType())).collect(Collectors.toList())));
    assertThat(taskList).hasSize(1);

    var feedTask = taskList.get(0);
    feedTask.getSubTasks().forEach(subTask ->
        employeeService.completeSubTask(subTask.getSubTaskId(), subTask.getParent().getEmployee()));
    assertThat(taskRepository.findAllByIsComplete(false)).isEmpty();
  }


  void checkBodyStatus() {
    feedPigs(); //1
    var body = bodyService.getBodyByBarcode(barCode).orElseThrow();
    assertThat(body.getState()).isEqualTo(BodyState.FED);
  }
}
