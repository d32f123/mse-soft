package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface EmployeeRepository extends CrudRepository<Employee, UUID> {
}
