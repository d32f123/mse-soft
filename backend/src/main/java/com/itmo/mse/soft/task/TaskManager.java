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
import com.itmo.mse.soft.service.PigstyService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
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
    @Autowired
    PigstyService pigstyService;

    private final Map<TaskType, Duration> taskDurationMap = new HashMap<>();
    private final Map<SubTaskType, Duration> subTaskDurationMap = new HashMap<>();
    private final Map<TaskType, List<SubTaskType>> taskToSubTaskMap = new HashMap<>();
    private final Map<TaskType, EmployeeRole> taskToRoleMap = new HashMap<>();
    private final Map<TaskType, TaskType> taskTransitionMap = new HashMap<>();

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

        taskTransitionMap.put(TaskType.PICKUP, TaskType.GROOM);
        taskTransitionMap.put(TaskType.GROOM, TaskType.FEED);
        taskTransitionMap.put(TaskType.FEED, null);
        taskTransitionMap.put(TaskType.REGULAR_FEED, null);
    }

    private Task buildTask(Instant start, TaskType taskType, Employee employee, Pigsty pigsty) {
        var duration = taskDurationMap.get(taskType);
        var subTaskTypes = taskToSubTaskMap.get(taskType);
        var task = Task.builder()
                .employee(employee)
                .scheduleEntry(
                        ScheduleEntry.builder()
                                .timeStart(start)
                                .timeEnd(start.plus(duration)).build()
                ).isComplete(false)
                .taskType(taskType)
                .pigsty(pigsty)
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
                                    .timeEnd(start.plus(subOffset).plus(subTaskDuration)).build()
                    ).build();
            task.getSubTasks().add(subTask);
            subOffset = subOffset.plus(subTaskDuration);
        }

        return task;
    }

    private Task buildTaskAt(Instant start, TaskType taskType) {
        var duration = taskDurationMap.get(taskType);
        var role = taskToRoleMap.get(taskType);
        var employeeMap = getEmployeeMapByRole(role);
        if (employeeMap.isEmpty()) {
            log.warn("No employees of role '{}', cannot schedule task of type '{}'", role, taskType);
            return null;
        }
        Map<UUID, Pigsty> pigstyMap = taskType.isFeedingTask ? getPigstyMap() : Collections.emptyMap();
        if (taskType.isFeedingTask && pigstyMap.isEmpty()) {
            log.warn("No pigsties found, cannot schedule task of type '{}'", taskType);
        }
        var eventQueue = buildEventQueue(start, start.plus(duration), role);

        var availableEmployees = new HashSet<>(employeeMap.keySet());
        Set<UUID> availablePigsties = taskType.isFeedingTask ? new HashSet<>(pigstyMap.keySet()) : Collections.emptySet();
        for (var instant: eventQueue) {
            if (instant.instant.isAfter(start.plus(duration))) {
                break;
            }
            if (instant.newTask) {
                availableEmployees.remove(instant.currentEmployee);
                if (instant.pigstyId != null && taskType.isFeedingTask) {
                    availablePigsties.remove(instant.pigstyId);
                }
            }
            if (availableEmployees.isEmpty() || taskType.isFeedingTask && availablePigsties.isEmpty()) {
                return null;
            }
        }
        if (availableEmployees.isEmpty() || taskType.isFeedingTask && availablePigsties.isEmpty()) {
            return null;
        }
        return buildTask(start, taskType, employeeMap.get(availableEmployees.iterator().next()),
                taskType.isFeedingTask ? pigstyMap.get(availablePigsties.iterator().next()) : null);
    }


    private Task buildTaskSomewhere(Instant start, TaskType taskType) {
        var role = taskToRoleMap.get(taskType);
        var employeeMap = getEmployeeMapByRole(role);
        if (employeeMap.isEmpty()) {
            log.warn("No employees of role '{}', cannot schedule task '{}'", role, taskType);
            return null;
        }

        Map<UUID, Pigsty> pigstyMap = taskType.isFeedingTask ? getPigstyMap() : Collections.emptyMap();
        if (taskType.isFeedingTask && pigstyMap.isEmpty()) {
            log.warn("No pigsties found, cannot schedule task of type '{}'", taskType);
        }

        var eventQueue = buildEventQueue(
                start,
                LocalDateTime.of(2070, 1, 1, 0, 0).toInstant(ZoneOffset.UTC),
                role);
        var duration = taskDurationMap.get(taskType);

        var foundStart = start.plus(Duration.ZERO);
        Set<UUID> possibleEmployees = new HashSet<>(employeeMap.keySet());
        Set<UUID> possiblePigsties = taskType.isFeedingTask ? new HashSet<>(pigstyMap.keySet()) : Collections.emptySet();
        for (var instant : eventQueue) {
            if (instant.instant.isBefore(start)) {
                possibleEmployees.remove(instant.currentEmployee);
                if (instant.pigstyId != null && taskType.isFeedingTask) {
                    possiblePigsties.remove(instant.pigstyId);
                }
                continue;
            }
            if (foundStart.plus(duration).isBefore(instant.instant) && !possibleEmployees.isEmpty()
                 && (!taskType.isFeedingTask || !possiblePigsties.isEmpty())) {
                break;
            }
            if (instant.newTask) {
                possibleEmployees.remove(instant.currentEmployee);
                if (instant.pigstyId != null && taskType.isFeedingTask) {
                    possiblePigsties.remove(instant.pigstyId);
                }
                continue;
            }
            if (instant.currentEmployee != null) {
                possibleEmployees.add(instant.currentEmployee);
            }
            if (instant.pigstyId != null && taskType.isFeedingTask) {
                possiblePigsties.add(instant.pigstyId);
            }
            if (possibleEmployees.size() == 1 || possiblePigsties.size() == 1) {
                foundStart = instant.instant;
            }
        }
        assert possibleEmployees.size() != 0;
        assert !taskType.isFeedingTask || !possiblePigsties.isEmpty();
        var chosenEmployee = employeeMap.get(possibleEmployees.iterator().next());
        var chosenPigsty = taskType.isFeedingTask ? pigstyMap.get(possiblePigsties.iterator().next()) : null;

        return buildTask(foundStart, taskType, chosenEmployee, chosenPigsty);
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
        if (task == null) {
            return null;
        }

        task.setBody(body);
        return taskRepository.save(task);
    }

    public Task scheduleFeedingTaskAt(Instant start, Pigsty pigsty) {
        var task = buildTaskAt(start, TaskType.REGULAR_FEED);
        if (task == null) {
            return null;
        }

        task.setPigsty(pigsty);
        return taskRepository.save(task);
    }

    public Task scheduleFeedingTask(Pigsty pigsty) {
        return scheduleFeedingTask(pigsty, Instant.now());
    }

    public Task scheduleFeedingTask(Pigsty pigsty, Instant start) {
        var task = buildTaskSomewhere(start, TaskType.REGULAR_FEED);
        if (task == null) {
            return null;
        }

        task.setPigsty(pigsty);
        return taskRepository.save(task);
    }

    public List<Task> getDailyTasks(UUID employeeId) {
        return getDailyTasks(employeeId, LocalDate.now());
    }

    public List<Task> getDailyTasks(UUID employeeId, LocalDate day) {
        Instant dayInstant = day.atStartOfDay().toInstant(ZoneOffset.UTC);
        Duration nextDay = Duration.ofDays(1);

        return taskRepository.findIntersectionsByEmployeeIdAndTime(employeeId, dayInstant, dayInstant.plus(nextDay));
    }

    public Task completeTask(UUID taskId, UUID employeeId) {
        var task = taskRepository.findById(taskId).orElseThrow();
        if (!task.getEmployee().getEmployeeId().equals(employeeId)) {
            return null;
        }
        if (task.isComplete()) {
            log.error("Task '{}' is already complete", taskId);
            return task;
        }

        task.setComplete(true);
        task.getSubTasks().forEach(subTask -> subTask.setComplete(true));
        taskRepository.save(task);

        if (task.getTaskType().isFeedingTask) {
            log.debug("Task '{}' marked at pigsty '{}'", task.getTaskId(), task.getPigsty().getPigstyId());
            pigstyService.feedPigsty(task);
        }


        var nextTaskType = taskTransitionMap.get(task.getTaskType());
        if (nextTaskType == null) {
            log.debug("Task '{}' is complete and no further tasks are required", taskId);
            return task;
        }

        assert task.getBody() != null;
        var scheduledTask = this.scheduleTask(nextTaskType, task.getBody());
        log.debug("Task '{}' is complete", taskId);
        if (scheduledTask != null) {
            log.debug("Scheduled task '{}' for employee '{}'", scheduledTask.getTaskId(),
                    scheduledTask.getEmployee().getName());
        }
        return task;
    }

    public Task completeSubTask(UUID subTaskId, UUID employeeId) {
        var subTask = subTaskRepository.findById(subTaskId).orElseThrow();
        var task = subTask.getParent();

        if (!task.getEmployee().getEmployeeId().equals(employeeId)) {
            return null;
        }

        if (subTask.isComplete()) {
            log.error("SubTask '{}' is already complete", subTaskId);
            return task;
        }

        subTask.setComplete(true);
        if (task.getSubTasks().stream().allMatch(SubTask::isComplete)) {
            task.setComplete(true);

            if (task.getTaskType().isFeedingTask) {
                pigstyService.feedPigsty(task);
            }

            var newTaskType = taskTransitionMap.get(task.getTaskType());
            if (newTaskType == null) {
                log.debug("SubTask '{}' complete, Task '{}' complete. No further tasks",
                        subTaskId, task.getTaskId());
            } else {
                var scheduledTask = this.scheduleTask(newTaskType, task.getBody());
                log.debug("SubTask '{}' complete, Task '{}' complete. Scheduled task: '{}'",
                        subTaskId, task.getTaskId(), scheduledTask.getTaskId());
            }
        } else {
            log.debug("SubTask '{}' complete, Task '{}' is not yet",
                    subTaskId, task.getTaskId());
        }

        return taskRepository.save(task);
    }
    @Builder
    private static class InstantWithMeta {
        public Instant instant;
        public UUID currentEmployee;
        public boolean newTask;
        public UUID pigstyId;
    }

    private PriorityQueue<InstantWithMeta> buildEventQueue(Instant start, Instant to, EmployeeRole employeeRole) {
        var eventQueue = new PriorityQueue<InstantWithMeta>(Comparator.comparing(x -> x.instant));
        taskRepository.findIntersectionsByEmployeeRoleAndTime(employeeRole, start, to)
                .forEach(task -> {
                    eventQueue.add(InstantWithMeta.builder()
                            .instant(task.getScheduleEntry().getTimeStart())
                            .currentEmployee(task.getEmployee().getEmployeeId())
                            .pigstyId(task.getPigsty() != null ? task.getPigsty().getPigstyId() : null)
                            .newTask(true).build());
                    eventQueue.add(InstantWithMeta.builder()
                            .instant(task.getScheduleEntry().getTimeEnd())
                            .currentEmployee(task.getEmployee().getEmployeeId())
                            .pigstyId(null)
                            .newTask(false).build());
                    if (task.getPigsty() != null) {
                        eventQueue.add(InstantWithMeta.builder()
                                .instant(task.getScheduleEntry().getTimeEnd().plus(Duration.ofDays(5)))
                                .pigstyId(task.getPigsty().getPigstyId())
                                .newTask(false).build());
                    }
                });
        return eventQueue;
    }

    private Map<UUID, Employee> getEmployeeMapByRole(EmployeeRole employeeRole) {
        var employeeMap = new HashMap<UUID, Employee>();
        employeeService.getEmployeesByRole(employeeRole)
                .forEach(employee -> employeeMap.put(employee.getEmployeeId(), employee));
        return employeeMap;
    }

    private Map<UUID, Pigsty> getPigstyMap() {
        var pigstyMap = new HashMap<UUID, Pigsty>();
        pigstyService.getAll().forEach(
                pigsty -> pigstyMap.put(pigsty.getPigstyId(), pigsty)
        );
        return pigstyMap;
    }

}
