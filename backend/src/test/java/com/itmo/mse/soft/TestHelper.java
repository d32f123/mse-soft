package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.Order;
import com.itmo.mse.soft.order.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TestHelper {

    public Body createBody() {
        return Body.builder()
                .payment(
                        Payment.builder()
                                .order(
                                        Order.builder()
                                        .paymentInstant(Instant.now())
                                        .paymentAmount(new BigDecimal("132.50")).build()
                                ).bitcoinAddress(UUID.randomUUID().toString())
                                .creationInstant(Instant.now()).build()
                ).barcode("askdjajskdajksldjaksdjk")
                .state(BodyState.IN_GROOMING).build();
    }

    public Employee createEmployee() {
        return Employee.builder()
                .name("Vasya")
                .employeeRole(EmployeeRole.GROOMER).build();
    }

    public Instant getTimeAt(int hours, int minutes) {
        return LocalDateTime.of(2020, 12, 5, hours, minutes).toInstant(ZoneOffset.UTC);
    }

    public Instant getDayAt(int day, int month, int year) {
        return LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
