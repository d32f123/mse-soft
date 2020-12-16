package com.itmo.mse.soft.api.frontend.v1;

import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.service.AuthService;
import com.itmo.mse.soft.service.EmployeeService;
import com.itmo.mse.soft.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        Employee employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(employeeService.getCurrentDailyTasks(employee));
    }

    @GetMapping("/get-role")
    public ResponseEntity<EmployeeRole> getRole(@RequestHeader(name = "Token") String token) {
        Employee employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(employee.getEmployeeRole());
    }

    @PostMapping("/complete-task/{taskId}")
    public ResponseEntity<Task> completeTask(
            @RequestHeader(name = "Token") String token,
            @PathVariable("taskId") UUID taskId
    ) {
        Employee employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Task task = employeeService.completeTask(taskId, employee);
        if (task == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/complete-sub-task/{subTaskId}")
    public ResponseEntity<Task> completeSubTask(
            @RequestHeader(name="Token") String token,
            @PathVariable("subTaskId") UUID subTaskId
    ) {
        Employee employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Task task = employeeService.completeSubTask(subTaskId, employee);
        if (task == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(task);
    }

}
