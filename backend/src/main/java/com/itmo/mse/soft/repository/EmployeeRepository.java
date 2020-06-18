package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends CrudRepository<Employee, UUID> {
    Optional<Employee> findByName(String name);
    List<Employee> findAllByEmployeeRole(EmployeeRole employeeRole);
}
