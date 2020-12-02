package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.*;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ReaderRepository;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
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
//        IntStream.range(0, 50)
//            .mapToObj(i -> generatePigsty())
//            .forEach(pigstyRepository::save);
//        IntStream.range(0, 50)
//            .mapToObj(i -> generateEmployee(EmployeeRole.PIG_MASTER))
//            .forEach(employeeRepository::save);
//        IntStream.range(0, 100)
//            .mapToObj(i -> generateEmployee(EmployeeRole.GROOMER))
//            .forEach(employeeRepository::save);
        readerRepository.saveAll(readers);
        employeeRepository.saveAll(employees);
        pigstyRepository.saveAll(pigsties);
    }

    Pigsty generatePigsty(){
        return Pigsty.builder()
            .pigAmount(5)
            .pigstyNumber(new Random().nextInt(100000))
            .build();
    }

    Employee generateEmployee(EmployeeRole employeeRole){
        return Employee.builder()
            .name("Oleg" + UUID.randomUUID().toString())
            .employeeRole(employeeRole)
            .build();
    }

}
