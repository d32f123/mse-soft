package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.task.Task;
import com.itmo.mse.soft.task.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService extends EntityService<Employee> {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TaskManager taskManager;

    @Override
    protected CrudRepository<Employee, UUID> getEntityRepository() {
        return employeeRepository;
    }

    public List<Employee> getEmployeesByRole(EmployeeRole employeeRole) {
        return employeeRepository.findAllByEmployeeRole(employeeRole);
    }

    public List<Task> getCurrentDailyTasks(Employee employee) {
        return taskManager.getDailyTasks(employee.getEmployeeId());
    }

    public Task completeTask(UUID taskId, Employee employee) {
        return taskManager.completeTask(taskId, employee.getEmployeeId());
    }

    public Task completeSubTask(UUID subTaskId, Employee employee) {
        return taskManager.completeSubTask(subTaskId, employee.getEmployeeId());
    }

}
