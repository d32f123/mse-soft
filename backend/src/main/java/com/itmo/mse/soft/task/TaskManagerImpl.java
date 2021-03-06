package com.itmo.mse.soft.task;

import static com.itmo.mse.soft.entity.BodyState.AWAITING_RECEIVAL;
import static com.itmo.mse.soft.entity.BodyState.FED;
import static com.itmo.mse.soft.entity.BodyState.GROOMED;
import static com.itmo.mse.soft.entity.BodyState.IN_FEEDING;
import static com.itmo.mse.soft.entity.BodyState.IN_RECEIVAL;
import static com.itmo.mse.soft.task.TaskType.PICKUP;

import com.itmo.mse.soft.api.hydra.OrderAPI;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.entity.Pigsty;
import com.itmo.mse.soft.entity.ReaderLocation;
import com.itmo.mse.soft.repository.SubTaskRepository;
import com.itmo.mse.soft.repository.TaskRepository;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.schedule.ScheduleManager;
import com.itmo.mse.soft.service.EmployeeService;
import com.itmo.mse.soft.service.PigstyService;
import com.itmo.mse.soft.service.ReaderService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class TaskManagerImpl implements TaskManager {

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
  @Autowired
  ReaderService readerService;

  private final ConcurrentMap<UUID, ReentrantLock> taskLocks = new ConcurrentHashMap<>();

  @Override
  public boolean scheduleBody(Instant pickupInstant, Body body) {
    Task initialTask = schedulePickupTask(pickupInstant, body);
    if (initialTask == null) {
      return false;
    }

    Task currentTask = initialTask;
    while (taskTransitionMap.containsKey(currentTask.getTaskType())) {
      currentTask = scheduleTask(
          taskTransitionMap.get(currentTask.getTaskType()),
          body,
          currentTask.getScheduleEntry().getTimeEnd()
      );
      if (currentTask != null) {
        continue;
      }
      log.error("Could not completely schedule body '{}'", body);
      return false;
    }

    return true;
  }

  private Task completeTask(Task task) {
    task.setComplete(true);
    task.getBody().setState(bodyStateMap.get(task.getTaskType()).atEnd);
    taskRepository.saveAndFlush(task);

    if (task.getTaskType().equals(PICKUP)) {
      task.getBody().setBarcode(issueBarcode());
    }

    if (task.getTaskType().isFeedingTask) {
      log.debug("Task '{}' marked at pigsty '{}'", task.getTaskId(), task.getPigsty().getPigstyId());
      pigstyService.feedPigsty(task);
    }

    log.debug("Task '[{}] {}' is complete", task.getTaskId(), task.getTaskType());
    return task;
  }

  private void acquireTaskLock(UUID taskId) {
    taskLocks.computeIfAbsent(taskId, (k) -> new ReentrantLock()).lock();
    log.debug("Acquired lock for '{}'", taskId);
  }

  private void releaseTaskLock(UUID taskId) {
    taskLocks.get(taskId).unlock();
    log.debug("Released lock for '{}'", taskId);
  }

  @Override
  public synchronized Task completeTask(UUID taskId, UUID employeeId) {
    Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException());
    if (!task.getEmployee().getEmployeeId().equals(employeeId)) {
      return null;
    }
    if (task.isComplete()) {
      log.error("Task '{}' is already complete", taskId);
      return task;
    }

    task.getSubTasks().forEach(subTask -> subTask.setComplete(true));
    completeTask(task);
    return task;
  }

  @Override
  public synchronized Task completeSubTask(UUID subTaskId, UUID employeeId) {
    SubTask subTask = subTaskRepository.findById(subTaskId).orElseThrow(() -> new RuntimeException());
    Task task = subTask.getParent();
    if (!task.getEmployee().getEmployeeId().equals(employeeId)) {
      return null;
    }
    if (subTask.isComplete()) {
      log.error("SubTask '{}' is already complete", subTaskId);
      return task;
    }
    switch (subTask.getSubTaskType()) {
      case PRINT_BARCODE:
        subTask.getParent().getBody().setBarcode(issueBarcode());
        break;
      case PUT_IN_FRIDGE:
        readerService.scanBarcode(ReaderLocation.AT_FRIDGE_ENTRANCE, task.getBody().getBarcode());
        break;
      case TAKE_FROM_FRIDGE:
        readerService.scanBarcode(ReaderLocation.AT_FRIDGE_EXIT, task.getBody().getBarcode());
        break;
      case TAKE_OUT_TEETH:
        task.getBody().setTeethTakenOut(true);
        break;
      case SHAVE:
        task.getBody().setShaved(true);
        break;
      case BUTCHER:
        task.getBody().setButched(true);
        break;
      default:
        break;
    }

    subTask.setComplete(true);
    task.getBody().setState(bodyStateMap.get(task.getTaskType()).atStart);
    if (task.getSubTasks().stream().allMatch(SubTask::isComplete)) {
      log.debug("SubTask '[{}] {}' complete", subTaskId, subTask.getSubTaskType());
      return completeTask(task);
    }
    log.debug("SubTask '[{}] {}' complete, Task '[{}] {}' is not yet",
        subTaskId, subTask.getSubTaskType(), task.getTaskId(), task.getTaskType());

    return taskRepository.saveAndFlush(task);
  }

  public String issueBarcode() {
    log.debug("Issuing barcode");

    return UUID.randomUUID().toString();
  }

  @Override
  public Task scheduleFeedingTaskAt(Instant start, Pigsty pigsty) {
    Task task = buildTaskAt(start, TaskType.REGULAR_FEED);
    if (task == null) {
      return null;
    }

    task.setPigsty(pigsty);
    return taskRepository.saveAndFlush(task);
  }

  @Override
  public Task scheduleFeedingTask(Pigsty pigsty) {
    return scheduleFeedingTask(pigsty, Instant.now());
  }

  @Override
  public Task scheduleFeedingTask(Pigsty pigsty, Instant start) {
    Task task = buildTaskSomewhere(start, TaskType.REGULAR_FEED);
    if (task == null) {
      return null;
    }

    task.setPigsty(pigsty);
    return taskRepository.saveAndFlush(task);
  }

  @Override
  public List<Task> getDailyTasks(UUID employeeId) {
    return getDailyTasks(employeeId, LocalDate.now());
  }

  @Override
  public List<Task> getDailyTasks(UUID employeeId, LocalDate day) {
    Instant dayInstant = day.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    Duration nextDay = Duration.ofDays(1);

    List<Task> dailyTasks = taskRepository
        .findIntersectionsByEmployeeIdAndTime(employeeId, dayInstant, dayInstant.plus(nextDay));
    boolean isGroomer = employeeService.getById(employeeId).orElseThrow(
        () -> new RuntimeException()
    ).getEmployeeRole().equals(EmployeeRole.GROOMER);
    if (isGroomer) {
      List<BodyState> possible = new ArrayList<>();
      possible.add(AWAITING_RECEIVAL);
      possible.add(IN_RECEIVAL);

      List<Task> goodDailyTasks = dailyTasks.stream()
          .filter(task -> (task.getBody().getPayment().getBodyOrder().getPaymentAmount().intValue() == 0))
          .filter(task ->
              (!possible.contains(task.getBody().getState())) ||
                  (possible.contains(task.getBody().getState()) && PICKUP.equals(task.getTaskType()))

          )
          .collect(Collectors.toList());
      return goodDailyTasks;
    }
    else {
      List<BodyState> possible = new ArrayList<>();
      possible.add(IN_FEEDING);
      possible.add(GROOMED);
      List<Task> goodDailyTasks = dailyTasks.stream()
          .filter(task -> (possible.contains(task.getBody().getState())))
          .collect(Collectors.toList());
      return goodDailyTasks;
    }
  }

  private Task buildTask(Instant start, TaskType taskType, Employee employee, Pigsty pigsty) {
    Duration duration = taskDurationMap.get(taskType);
    List<SubTaskType> subTaskTypes = taskToSubTaskMap.get(taskType);
    Task task = Task.builder()
        .employee(employee)
        .scheduleEntry(
            ScheduleEntry.builder()
                .timeStart(start)
                .timeEnd(start.plus(duration)).build()
        ).isComplete(false)
        .taskType(taskType)
        .pigsty(pigsty)
        .subTasks(new ArrayList<>()).build();

    Duration subOffset = Duration.ZERO;
    for (SubTaskType subTaskType : subTaskTypes) {
      Duration subTaskDuration = subTaskDurationMap.get(subTaskType);
      SubTask subTask = SubTask.builder()
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
    Duration duration = taskDurationMap.get(taskType);
    EmployeeRole role = taskToRoleMap.get(taskType);
    Map<UUID, Employee> employeeMap = getEmployeeMapByRole(role);
    if (employeeMap.isEmpty()) {
      log.warn("No employees of role '{}', cannot schedule task of type '{}'", role, taskType);
      return null;
    }
    Map<UUID, Pigsty> pigstyMap = taskType.isFeedingTask ? getPigstyMap() : Collections.emptyMap();
    if (taskType.isFeedingTask && pigstyMap.isEmpty()) {
      log.warn("No pigsties found, cannot schedule task of type '{}'", taskType);
    }
    Queue<InstantWithMeta> eventQueue = buildEventQueue(start, start.plus(duration), role);

    HashSet<UUID> availableEmployees = new HashSet<>(employeeMap.keySet());
    Set<UUID> availablePigsties = taskType.isFeedingTask ? new HashSet<>(pigstyMap.keySet()) : Collections.emptySet();
    while (!eventQueue.isEmpty()) {
      InstantWithMeta instant = eventQueue.poll();
      if (instant.instant.isAfter(start.plus(duration))) {
        break;
      }
      if (instant.instant.equals(start) && !instant.newTask) {
        // Case when some other task ends on the border with this tasks. Return objects to pools
        availableEmployees.add(instant.currentEmployee);
        if (instant.pigstyId != null && taskType.isFeedingTask) {
          availablePigsties.add(instant.pigstyId);
        }
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
    EmployeeRole role = taskToRoleMap.get(taskType);
    Map<UUID, Employee> employeeMap = getEmployeeMapByRole(role);
    if (employeeMap.isEmpty()) {
      log.warn("No employees of role '{}', cannot schedule task '{}'", role, taskType);
      return null;
    }

    Map<UUID, Pigsty> pigstyMap = taskType.isFeedingTask ? getPigstyMap() : Collections.emptyMap();
    if (taskType.isFeedingTask && pigstyMap.isEmpty()) {
      log.warn("No pigsties found, cannot schedule task of type '{}'", taskType);
    }

    Queue<InstantWithMeta> eventQueue = buildEventQueue(
        start,
        LocalDateTime.of(2070, 1, 1, 0, 0).toInstant(ZoneOffset.UTC),
        role);
    Duration duration = taskDurationMap.get(taskType);

    Instant foundStart = start.plus(Duration.ZERO);
    Set<UUID> possibleEmployees = new HashSet<>(employeeMap.keySet());
    Set<UUID> possiblePigsties = taskType.isFeedingTask ? new HashSet<>(pigstyMap.keySet()) : Collections.emptySet();
    while (!eventQueue.isEmpty()) {
      InstantWithMeta instant = eventQueue.poll();
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
      if (instant.instant.isAfter(foundStart) && (possibleEmployees.size() == 1 || possiblePigsties.size() == 1)) {
        foundStart = instant.instant;
      }
    }
    assert possibleEmployees.size() != 0;
    assert !taskType.isFeedingTask || !possiblePigsties.isEmpty();
    Employee chosenEmployee = employeeMap.get(possibleEmployees.iterator().next());
    Pigsty chosenPigsty = taskType.isFeedingTask ? pigstyMap.get(possiblePigsties.iterator().next()) : null;

    return buildTask(foundStart, taskType, chosenEmployee, chosenPigsty);
  }

  private Task schedulePickupTask(Instant start, Body body) {
    Task task = buildTaskAt(start, PICKUP);

    if (task == null) {
      return null;
    }

    task.setBody(body);
    return taskRepository.saveAndFlush(task);
  }

  private Task scheduleTask(TaskType taskType, Body body) {
    return scheduleTask(taskType, body, Instant.now());
  }

  private Task scheduleTask(TaskType taskType, Body body, Instant searchFrom) {
    Task task = buildTaskSomewhere(searchFrom, taskType);
    if (task == null) {
      return null;
    }

    task.setBody(body);
    return taskRepository.saveAndFlush(task);
  }

  @Builder
  private static class InstantWithMeta {

    public Instant instant;
    public UUID currentEmployee;
    public boolean newTask;
    public UUID pigstyId;
  }

  private Queue<InstantWithMeta> buildEventQueue(Instant start, Instant to, EmployeeRole employeeRole) {
    PriorityBlockingQueue<InstantWithMeta> eventQueue = new PriorityBlockingQueue<InstantWithMeta>(20, Comparator.comparing(x -> x.instant));
    Duration pigstyCoolDown = pigstyService.getPigstyCoolDownDuration();
    Instant actualStart = employeeRole == EmployeeRole.PIG_MASTER ? start.minus(pigstyCoolDown) : start;
    taskRepository.findIntersectionsByEmployeeRoleAndTime(employeeRole, actualStart, to)
        .forEach(task -> {
          eventQueue.add(InstantWithMeta.builder()
              .instant(task.getScheduleEntry().getTimeStart())
              .currentEmployee(task.getEmployee().getEmployeeId())
              .pigstyId(task.getPigsty() != null ? task.getPigsty().getPigstyId() : null)
              .newTask(true).build());
          // Employee is available
          eventQueue.add(InstantWithMeta.builder()
              .instant(task.getScheduleEntry().getTimeEnd())
              .currentEmployee(task.getEmployee().getEmployeeId())
              .pigstyId(null)
              .newTask(false).build());
          if (task.getPigsty() != null) {
            // Pigsty is available only pigstyCoolDown days after
            eventQueue.add(InstantWithMeta.builder()
                .instant(task.getScheduleEntry().getTimeEnd().plus(pigstyCoolDown))
                .pigstyId(task.getPigsty().getPigstyId())
                .newTask(false).build());
          }
        });
    return eventQueue;
  }

  private Map<UUID, Employee> getEmployeeMapByRole(EmployeeRole employeeRole) {
    HashMap<UUID, Employee> employeeMap = new HashMap<UUID, Employee>();
    employeeService.getEmployeesByRole(employeeRole)
        .forEach(employee -> employeeMap.put(employee.getEmployeeId(), employee));
    return employeeMap;
  }

  private Map<UUID, Pigsty> getPigstyMap() {
    HashMap<UUID, Pigsty> pigstyMap = new HashMap<UUID, Pigsty>();
    pigstyService.getAll().forEach(
        pigsty -> pigstyMap.put(pigsty.getPigstyId(), pigsty)
    );
    return pigstyMap;
  }

  @Builder
  private static class StateTransition {

    public BodyState atStart;
    public BodyState atEnd;
  }

  private final Map<TaskType, Duration> taskDurationMap = new HashMap<>();
  private final Map<SubTaskType, Duration> subTaskDurationMap = new HashMap<>();
  private final Map<TaskType, StateTransition> bodyStateMap = new HashMap<>();
  private final Map<TaskType, List<SubTaskType>> taskToSubTaskMap = new HashMap<>();
  private final Map<TaskType, EmployeeRole> taskToRoleMap = new HashMap<>();
  private final Map<TaskType, TaskType> taskTransitionMap = new HashMap<>();

  public TaskManagerImpl() {
    // Pickup: PICKUP_FROM_CUSTOMER (30) + PRINT_BARCODE(15) + PUT_IN_FRIDGE(15) = 1 HOUR
    // Groom: TAKE_FROM_FRIDGE(15) + TAKE_OUT_TEETH(45) + SHAVE(15) + BUTCHER(30) + PUT_IN_FRIDGE(15) = 2 HOURS
    // Feed: TAKE_FROM_FRIDGE(15) + FEED(45) = 1 Hour
    // Regular feed: 45 minutes
//        taskDurationMap.put(TaskType.PICKUP, Duration.ofMinutes(60));
//        taskDurationMap.put(TaskType.GROOM, Duration.ofHours(2));
//        taskDurationMap.put(TaskType.FEED, Duration.ofHours(1));
//        taskDurationMap.put(TaskType.REGULAR_FEED, Duration.ofMinutes(45));
//
//        subTaskDurationMap.put(SubTaskType.PICKUP_FROM_CUSTOMER, Duration.ofMinutes(30));
//        subTaskDurationMap.put(SubTaskType.PRINT_BARCODE, Duration.ofMinutes(15));
//        subTaskDurationMap.put(SubTaskType.PUT_IN_FRIDGE, Duration.ofMinutes(15));
//
//        subTaskDurationMap.put(SubTaskType.TAKE_FROM_FRIDGE, Duration.ofMinutes(15));
//        subTaskDurationMap.put(SubTaskType.TAKE_OUT_TEETH, Duration.ofMinutes(45));
//        subTaskDurationMap.put(SubTaskType.SHAVE, Duration.ofMinutes(15));
//        subTaskDurationMap.put(SubTaskType.BUTCHER, Duration.ofMinutes(30));
//
//        subTaskDurationMap.put(SubTaskType.FEED, Duration.ofMinutes(45));
    taskDurationMap.put(PICKUP, Duration.ofSeconds(4));
    taskDurationMap.put(TaskType.GROOM, Duration.ofSeconds(8));
    taskDurationMap.put(TaskType.FEED, Duration.ofSeconds(4));
    taskDurationMap.put(TaskType.REGULAR_FEED, Duration.ofSeconds(3));

    subTaskDurationMap.put(SubTaskType.PICKUP_FROM_CUSTOMER, Duration.ofSeconds(2));
    subTaskDurationMap.put(SubTaskType.PRINT_BARCODE, Duration.ofSeconds(1));
    subTaskDurationMap.put(SubTaskType.PUT_IN_FRIDGE, Duration.ofSeconds(1));

    subTaskDurationMap.put(SubTaskType.TAKE_FROM_FRIDGE, Duration.ofSeconds(1));
    subTaskDurationMap.put(SubTaskType.TAKE_OUT_TEETH, Duration.ofSeconds(3));
    subTaskDurationMap.put(SubTaskType.SHAVE, Duration.ofSeconds(1));
    subTaskDurationMap.put(SubTaskType.BUTCHER, Duration.ofSeconds(2));

    subTaskDurationMap.put(SubTaskType.FEED, Duration.ofSeconds(3));

    bodyStateMap.put(PICKUP, StateTransition.builder()
        .atStart(IN_RECEIVAL)
        .atEnd(BodyState.RECEIVED)
        .build());
    bodyStateMap.put(TaskType.GROOM, StateTransition.builder()
        .atStart(BodyState.IN_GROOMING)
        .atEnd(GROOMED)
        .build());
    bodyStateMap.put(TaskType.FEED, StateTransition.builder()
        .atStart(IN_FEEDING)
        .atEnd(FED)
        .build());

    taskToSubTaskMap.put(PICKUP, Arrays.asList(
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

    taskToRoleMap.put(PICKUP, EmployeeRole.GROOMER);
    taskToRoleMap.put(TaskType.GROOM, EmployeeRole.GROOMER);
    taskToRoleMap.put(TaskType.FEED, EmployeeRole.PIG_MASTER);
    taskToRoleMap.put(TaskType.REGULAR_FEED, EmployeeRole.PIG_MASTER);

    taskTransitionMap.put(PICKUP, TaskType.GROOM);
    taskTransitionMap.put(TaskType.GROOM, TaskType.FEED);

    log.info("CREATED");
  }
}
