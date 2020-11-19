package com.itmo.mse.soft.db;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.order.Order;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.OrderRepository;
import com.itmo.mse.soft.repository.PaymentRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ScheduleEntryRepository;
import com.itmo.mse.soft.repository.SubTaskRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.task.SubTask;
import com.itmo.mse.soft.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
public class DBConstraintTests {

  @Autowired
  private BodyRepository bodyRepository;
  @Autowired
  private EmployeeRepository employeeRepository;
  @Autowired
  PaymentRepository paymentRepository;
  @Autowired
  private TaskRepository taskRepository;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private SubTaskRepository subTaskRepository;
  @Test
  void paymentConstraint(){
    Payment payment = Payment.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> paymentRepository.save(payment));
  }

  @Test
  void taskConstraint(){
    Task task = Task.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> taskRepository.save(task));
  }

  @Test
  void orderConstraint(){
    Order order = Order.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> orderRepository.save(order));
  }

  @Test
  void bodyConstraint(){
    Body body = Body.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> bodyRepository.save(body));
  }

  @Test
  void employeeConstraint(){
    Employee employee = Employee.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> employeeRepository.save(employee));
  }

  @Test
  void subTaskRepositoryConstraint(){
    SubTask subTask = SubTask.builder().build();
    Assertions.assertThrows(DataIntegrityViolationException.class, () -> subTaskRepository.save(subTask));
  }
}
