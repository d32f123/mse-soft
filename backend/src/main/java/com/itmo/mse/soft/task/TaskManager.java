package com.itmo.mse.soft.task;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.repository.SubTaskRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.schedule.ScheduleManager;
import com.itmo.mse.soft.service.BodyService;
import com.itmo.mse.soft.service.EmployeeService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class TaskManager {

    @Autowired
    BodyService bodyService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    ScheduleManager scheduleManager;
    @Autowired
    OrderAPI orderAPI;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    SubTaskRepository subTaskRepository;

    private final Map<TaskType, Duration> taskDurationMap = new HashMap<>();
    private final Map<SubTaskType, Duration> subTaskDurationMap = new HashMap<>();
    private final Map<TaskType, List<SubTaskType>> taskToSubTaskMap = new HashMap<>();
    private final Map<TaskType, EmployeeRole> taskToRoleMap = new HashMap<>();

    public TaskManager() {
        // Pickup: PICKUP_FROM_CUSTOMER (30) + PRINT_BARCODE(15) + PUT_IN_FRIDGE(15) = 1 HOUR
        // Groom: TAKE_FROM_FRIDGE(15) + TAKE_OUT_TEETH(45) + SHAVE(15) + BUTCHER(30) + PUT_IN_FRIDGE(15) = 2 HOURS
        // Feed: TAKE_FROM_FRIDGE(15) + FEED(45) = 1 Hour
        // Regular feed: 45 minutes
        taskDurationMap.put(TaskType.PICKUP, Duration.ofMinutes(60));
        taskDurationMap.put(TaskType.GROOM, Duration.ofHours(2));
        taskDurationMap.put(TaskType.FEED, Duration.ofHours(1));
        taskDurationMap.put(TaskType.REGULAR_FEED, Duration.ofMinutes(45));

        subTaskDurationMap.put(SubTaskType.PICKUP_FROM_CUSTOMER, Duration.ofMinutes(30));
        subTaskDurationMap.put(SubTaskType.PRINT_BARCODE, Duration.ofMinutes(15));
        subTaskDurationMap.put(SubTaskType.PUT_IN_FRIDGE, Duration.ofMinutes(15));

        subTaskDurationMap.put(SubTaskType.TAKE_FROM_FRIDGE, Duration.ofMinutes(15));
        subTaskDurationMap.put(SubTaskType.TAKE_OUT_TEETH, Duration.ofMinutes(45));
        subTaskDurationMap.put(SubTaskType.SHAVE, Duration.ofMinutes(15));
        subTaskDurationMap.put(SubTaskType.BUTCHER, Duration.ofMinutes(30));

        subTaskDurationMap.put(SubTaskType.FEED, Duration.ofMinutes(45));

        taskToSubTaskMap.put(TaskType.PICKUP, Arrays.asList(
                SubTaskType.PICKUP_FROM_CUSTOMER,
                SubTaskType.PRINT_BARCODE,
                SubTaskType.PUT_IN_FRIDGE
        ));
        taskToSubTaskMap.put(TaskType.GROOM, Arrays.asList(
                SubTaskType.TAKE_FROM_FRIDGE,
                SubTaskType.TAKE_OUT_TEETH,
                SubTaskType.SHAVE,
                SubTaskType.BUTCHER,
                SubTaskType.PUT_IN_FRIDGE
        ));
        taskToSubTaskMap.put(TaskType.FEED, Arrays.asList(
                SubTaskType.TAKE_FROM_FRIDGE,
                SubTaskType.FEED
        ));
        taskToSubTaskMap.put(TaskType.REGULAR_FEED, Collections.emptyList());

        taskToRoleMap.put(TaskType.PICKUP, EmployeeRole.GROOMER);
        taskToRoleMap.put(TaskType.GROOM, EmployeeRole.GROOMER);
        taskToRoleMap.put(TaskType.FEED, EmployeeRole.PIG_MASTER);
        taskToRoleMap.put(TaskType.REGULAR_FEED, EmployeeRole.PIG_MASTER);
    }

    private Task buildTask(Instant start, TaskType taskType, Employee employee) {
        var duration = taskDurationMap.get(taskType);
        var subTaskTypes = taskToSubTaskMap.get(taskType);
        var task = Task.builder()
                .employee(employee)
                .scheduleEntry(
                        ScheduleEntry.builder()
                                .timeStart(start)
                                .timeEnd(start.plus(duration))
                                .subEntries(new ArrayList<>()).build()
                ).isComplete(false)
                .taskType(taskType)
                .subTasks(new ArrayList<>()).build();

        var subOffset = Duration.ZERO;
        for (var subTaskType: subTaskTypes) {
            var subTaskDuration = subTaskDurationMap.get(subTaskType);
            var subTask = SubTask.builder()
                    .parent(task)
                    .isComplete(false)
                    .subTaskType(subTaskType)
                    .scheduleEntry(
                            ScheduleEntry.builder()
                                    .timeStart(start.plus(subOffset))
                                    .timeEnd(start.plus(subOffset).plus(subTaskDuration))
                                    .parent(task.getScheduleEntry()).build()
                    ).build();
            task.getScheduleEntry().getSubEntries().add(subTask.getScheduleEntry());
            task.getSubTasks().add(subTask);
            subOffset = subOffset.plus(subTaskDuration);
        }

        return task;
    }

    private Task buildTaskAt(Instant start, TaskType taskType) {
        var duration = taskDurationMap.get(taskType);
        var role = taskToRoleMap.get(taskType);
        var eventQueue = buildEventQueue(start, start.plus(duration), taskType);
        var employeeMap = getEmployeeMapByRole(role);

        var availableEmployees = new HashSet<>(employeeMap.keySet());
        for (var instant: eventQueue) {
            if (instant.instant.isAfter(start.plus(duration)) && employeeMap.isEmpty()) {
                return null;
            }
            if (instant.instant.isAfter(start.plus(duration)) && !employeeMap.isEmpty()) {
                return buildTask(start, taskType, employeeMap.get(availableEmployees.iterator().next()));
            }
            if (instant.newTask) {
                availableEmployees.remove(instant.currentEmployee);
                continue;
            }
            availableEmployees.add(instant.currentEmployee);
        }
        assert !availableEmployees.isEmpty();
        return buildTask(start, taskType, employeeMap.get(availableEmployees.iterator().next()));
    }

    @Builder
    private static class InstantWithMeta {
        public Instant instant;
        public UUID currentEmployee;
        public boolean newTask;
    }

    private PriorityQueue<InstantWithMeta> buildEventQueue(Instant start, Instant to, TaskType taskType) {
        var employeeRole = taskToRoleMap.get(taskType);
        var eventQueue = new PriorityQueue<InstantWithMeta>(Comparator.comparing(x -> x.instant));
        taskRepository.findIntersectionsByEmployeeRoleAndTime(employeeRole, start, to)
                .forEach(task -> {
                    eventQueue.add(InstantWithMeta.builder()
                            .instant(task.getScheduleEntry().getTimeStart())
                            .currentEmployee(task.getEmployee().getEmployeeId())
                            .newTask(true).build());
                    eventQueue.add(InstantWithMeta.builder()
                            .instant(task.getScheduleEntry().getTimeEnd())
                            .currentEmployee(task.getEmployee().getEmployeeId())
                            .newTask(false).build());
                });
        return eventQueue;
    }

    private Map<UUID, Employee> getEmployeeMapByRole(EmployeeRole employeeRole) {
        var employeeMap = new HashMap<UUID, Employee>();
        employeeService.getEmployeesByRole(employeeRole)
                .forEach(employee -> employeeMap.put(employee.getEmployeeId(), employee));
        return employeeMap;
    }

    private Task buildTaskSomewhere(Instant start, TaskType taskType) {
        var role = taskToRoleMap.get(taskType);
        var employeeMap = getEmployeeMapByRole(role);

        var eventQueue = buildEventQueue(start, Instant.MAX, taskType);
        var duration = taskDurationMap.get(taskType);

        var foundStart = start.plus(Duration.ZERO);
        Set<UUID> possibleEmployees = new HashSet<>(employeeMap.keySet());
        Employee chosenEmployee = null;
        for (var instant : eventQueue) {
            if (instant.instant.isBefore(start)) {
                possibleEmployees.remove(instant.currentEmployee);
                continue;
            }
            if (foundStart.plus(duration).isBefore(instant.instant) && !possibleEmployees.isEmpty()) {
                chosenEmployee = employeeMap.get(possibleEmployees.iterator().next());
                break;
            }
            if (instant.newTask) {
                possibleEmployees.remove(instant.currentEmployee);
                continue;
            }
            possibleEmployees.add(instant.currentEmployee);
            if (possibleEmployees.size() == 1) {
                foundStart = instant.instant;
            }
        }
        assert chosenEmployee != null;

        return buildTask(foundStart, taskType, chosenEmployee);
    }

    public Task scheduleTaskAt(Instant start, TaskType taskType, Body body) {
        var task = buildTaskAt(start, taskType);

        if (task == null) {
            return task;
        }

        task.setBody(body);
        return taskRepository.save(task);
    }

    public Task scheduleTask(TaskType taskType, Body body) {
        return scheduleTask(taskType, body, Instant.now());
    }
    public Task scheduleTask(TaskType taskType, Body body, Instant searchFrom) {
        var task = buildTaskSomewhere(searchFrom, taskType);

        task.setBody(body);
        return taskRepository.save(task);
    }

    public Task scheduleFeedingTaskAt(Instant start, Pigsty pigsty) {
        var task = buildTaskAt(start, TaskType.REGULAR_FEED);
        if (task == null) {
            return task;
        }

        task.setPigsty(pigsty);
        return taskRepository.save(task);
    }

    public Task scheduleFeedingTask(Pigsty pigsty) {
        return scheduleFeedingTask(pigsty, Instant.now());
    }

    public Task scheduleFeedingTask(Pigsty pigsty, Instant start) {
        var task = buildTaskSomewhere(start, TaskType.REGULAR_FEED);

        task.setPigsty(pigsty);
        return taskRepository.save(task);
    }

    public Task completeTask(UUID taskId) {
        var task = taskRepository.findById(taskId).orElseThrow();

        return task;
    }

    public Task completeSubTask(UUID subTaskId) {
        var subTask = subTaskRepository.findById(subTaskId).orElseThrow();

        return subTask.getParent();
    }

}
