package com.itmo.mse.soft;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.repository.*;
import com.itmo.mse.soft.service.AuthService;
import com.itmo.mse.soft.service.AuthServiceTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BusinessProcess {

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

    @Test
    void loginBodyOwner(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void reserveSlot(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void logoutBodyOwner(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void loginGroomer(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void acceptBody(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void putBodyInFreeze(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void groomBody(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }
    @Test
    void logoutGroomer(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }
    @Test
    void loginPigMaster(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }
    @Test
    void feedPigs(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }
    @Test
    void logoutPigMaster(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void checkBodyStatus(){
        var employee = testHelper.createBodyOwner();
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }
}
