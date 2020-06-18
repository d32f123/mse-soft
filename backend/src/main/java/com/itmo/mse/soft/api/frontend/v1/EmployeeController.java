package com.itmo.mse.soft.api.frontend.v1;

import com.itmo.mse.soft.service.AuthService;
import com.itmo.mse.soft.service.EmployeeService;
import com.itmo.mse.soft.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Slf4j
public class EmployeeController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/daily-tasks")
    public ResponseEntity<List<Task>> getCurrentDailyTasks(@RequestHeader(name = "Token") String token) {
        var employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(employeeService.getCurrentDailyTasks(employee));
    }

}
