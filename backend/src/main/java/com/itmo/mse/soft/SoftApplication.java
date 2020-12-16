package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.*;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import com.itmo.mse.soft.repository.ReaderRepository;
import java.util.ArrayList;
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

    private final List<Employee> employees = new ArrayList<>();

    private final List<Pigsty> pigsties =  new ArrayList<>();

    private final List<Reader> readers = new ArrayList<>();


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
        employees.add(
            Employee.builder()
                .name("Shureek")
                .employeeRole(EmployeeRole.GROOMER)
                .password("Shureek")
                .build());
        employees.add(
            Employee.builder()
                .name("Dron")
                .employeeRole(EmployeeRole.GROOMER)
                .password("Dron")
                .build());
        employees.add(
            Employee.builder()
                .name("Oleg")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .password("Oleg")
                .build());

        pigsties.add(Pigsty.builder()
            .pigAmount(5)
            .pigstyNumber(1)
            .build());

        readers.add(
            Reader.builder()
                .location(ReaderLocation.AT_FRIDGE_ENTRANCE)
                .build());
        readers.add(
            Reader.builder()
                .location(ReaderLocation.AT_FRIDGE_EXIT)
                .build());

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
