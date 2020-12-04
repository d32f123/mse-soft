package com.itmo.mse.soft.service;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuthServiceTests {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    AuthService authService;

    @Autowired
    TestHelper testHelper;

    @Test
    void contextLoads() {}

    @Test
    void shouldAuthorize() {
        var employee = testHelper.createEmployee(EmployeeRole.GROOMER);
        employeeRepository.save(employee);

        assertThat(authService.authenticate(employee.getName())).isBase64();
    }

    @Test
    void shouldReturnEmployee() {
        var employee = testHelper.createEmployee(EmployeeRole.GROOMER);
        employeeRepository.save(employee);

        var token = authService.authenticate(employee.getName());
        assertThat(authService.getEmployeeByToken(token).getEmployeeId()).isEqualByComparingTo(employee.getEmployeeId());
    }

}
