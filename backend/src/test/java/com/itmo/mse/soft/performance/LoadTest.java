package com.itmo.mse.soft.performance;

import static com.itmo.mse.soft.task.SubTaskType.PICKUP_FROM_CUSTOMER;
import static com.itmo.mse.soft.task.SubTaskType.PRINT_BARCODE;
import static com.itmo.mse.soft.task.SubTaskType.PUT_IN_FRIDGE;
import static org.assertj.core.api.Assertions.assertThat;

import com.itmo.mse.soft.SoftApplication;
import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.OrderManager;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.OrderRepository;
import com.itmo.mse.soft.repository.PaymentRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ScheduleEntryRepository;
import com.itmo.mse.soft.repository.SubTaskRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.service.AuthService;
import com.itmo.mse.soft.service.BodyService;
import com.itmo.mse.soft.service.EmployeeService;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@RunWith(SpringRunner.class)
public class LoadTest extends AbstractBenchmark {

  private static EmployeeRepository employeeRepository;
  private static SubTaskRepository subTaskRepository;
  private static EmployeeService employeeService;
  private static OrderAPI orderAPI;
  private static OrderManager orderManager;
  static ConfigurableApplicationContext context;

  private static int iter = 0;
  @Setup(Level.Trial)
  public void setupBenchmark() {
    try {
      String args = "";
      if (context == null) {
        context = SpringApplication.run(SoftApplication.class, args);
      }
      employeeRepository = context.getBean(EmployeeRepository.class);
      subTaskRepository = context.getBean(SubTaskRepository.class);
      employeeService = context.getBean(EmployeeService.class);
      orderAPI = context.getBean(OrderAPI.class);
      orderManager = context.getBean(OrderManager.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Benchmark
  public void benchmarkFeedPigs() {
    feedPigs();
  }

  @SneakyThrows
  void reserveSlot() {
    System.out.println(++iter);

    orderManager.bodyOrderQueue.add(BodyOrder.builder()
        .paymentAmount(new BigDecimal("123.50"))
        .pickupInstant(Instant.now())
        .build());
    //wait end of processing
    orderManager.orderConsumer();
  }

  void loginGroomer() {
    reserveSlot();
    var employee = employeeRepository.findAllByEmployeeRole(EmployeeRole.GROOMER).get(0);
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
    employees.forEach(
        e -> taskList.addAll(employeeService.getCurrentDailyTasks(e)
            .stream()
            .filter(
                t -> !t.isComplete() && TaskType.PICKUP.equals(
                    t.getTaskType())).collect(Collectors.toList())));

    if (taskList.size() > 0) {
      var pickUpTask = taskList.get(0);
      var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
          .filter(subTask -> PICKUP_FROM_CUSTOMER.equals(subTask.getSubTaskType())).collect(Collectors.toList());

      var acceptBodyTask = acceptBodyTaskList.get(0);
      employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
      acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    }

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

    if (taskList.size() > 0) {
      var pickUpTask = taskList.get(0);
      var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
          .filter(subTask -> PRINT_BARCODE.equals(subTask.getSubTaskType())).collect(Collectors.toList());

      var acceptBodyTask = acceptBodyTaskList.get(0);
      employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
      acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    }
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

    if (taskList.size() > 0) {
      var pickUpTask = taskList.get(0);
      var acceptBodyTaskList = pickUpTask.getSubTasks().stream()
          .filter(subTask -> PUT_IN_FRIDGE.equals(subTask.getSubTaskType())).collect(Collectors.toList());

      var acceptBodyTask = acceptBodyTaskList.get(0);
      employeeService.completeSubTask(acceptBodyTask.getSubTaskId(), taskList.get(0).getEmployee());
      acceptBodyTask = subTaskRepository.findById(acceptBodyTask.getSubTaskId()).orElse(null);
    }


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

    if (taskList.size() > 0) {
      var groomTask = taskList.get(0);
      for (var sub : groomTask.getSubTasks()) {
        employeeService.completeSubTask(sub.getSubTaskId(), sub.getParent().getEmployee());
      }
    }

  }


  void loginPigMaster() {
    groomBody(); //1
    var employee = employeeRepository.findAllByEmployeeRole(EmployeeRole.PIG_MASTER).get(0);

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

    if (taskList.size() > 0) {
      var feedTask = taskList.get(0);
      feedTask.getSubTasks().forEach(subTask ->
          employeeService.completeSubTask(subTask.getSubTaskId(), subTask.getParent().getEmployee()));
    }

  }
}
