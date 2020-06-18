package com.itmo.mse.soft.task;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.schedule.ScheduleManager;
import com.itmo.mse.soft.service.BodyService;
import com.itmo.mse.soft.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskManager {

    @Autowired
    BodyService bodyService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    ScheduleManager scheduleManager;
    @Autowired
    OrderAPI orderAPI;

}
