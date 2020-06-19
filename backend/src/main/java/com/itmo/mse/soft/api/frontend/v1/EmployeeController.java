package com.itmo.mse.soft.api.frontend.v1;

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
        var employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(employeeService.getCurrentDailyTasks(employee));
    }

    @PostMapping("/complete-task/{taskId}")
    public ResponseEntity<Task> completeTask(
            @RequestHeader(name = "Token") String token,
            @PathVariable("taskId") UUID taskId
    ) {
        var employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        var task = employeeService.completeTask(taskId, employee);
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
        var employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        var task = employeeService.completeSubTask(subTaskId, employee);
        if (task == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(task);
    }

}
