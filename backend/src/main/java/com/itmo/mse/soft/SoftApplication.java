package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.*;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class SoftApplication {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PigstyRepository pigstyRepository;

    @Autowired
    ReaderRepository readerRepository;

    private final List<Employee> employees = List.of(
            Employee.builder()
            .name("Shureek")
            .employeeRole(EmployeeRole.GROOMER)
            .build(),
            Employee.builder()
            .name("Dron")
            .employeeRole(EmployeeRole.GROOMER)
            .build(),
            Employee.builder()
            .name("Oleg")
            .employeeRole(EmployeeRole.PIG_MASTER)
            .build()
    );

    private final List<Pigsty> pigsties = List.of(
            Pigsty.builder()
            .pigAmount(5)
            .pigstyNumber(1)
            .build()
//            Pigsty.builder()
//            .pigAmount(5)
//            .pigstyNumber(2)
//            .build()
    );

    private final List<Reader> readers = List.of(
            Reader.builder()
            .location(ReaderLocation.AT_FRIDGE_ENTRANCE)
            .build(),
            Reader.builder()
            .location(ReaderLocation.AT_FRIDGE_EXIT)
            .build()
    );


    public static void main(String[] args) {
        SpringApplication.run(SoftApplication.class, args);
    }

    @PostConstruct
    public void init() {
        employeeRepository.saveAll(employees);
        pigstyRepository.saveAll(pigsties);
        readerRepository.saveAll(readers);
    }

}
