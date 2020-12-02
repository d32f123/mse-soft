package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Service
public class TestHelper {

    public Body createBody() {
        return createBody(BodyState.IN_GROOMING);
    }
    public Body createBody(BodyState state) {
        return Body.builder()
                .payment(
                        Payment.builder()
                                .bodyOrder(
                                        BodyOrder.builder()
                                        .pickupInstant(Instant.now())
                                        .paymentAmount(new BigDecimal("132.50")).build()
                                ).bitcoinAddress(UUID.randomUUID().toString())
                                .creationInstant(Instant.now()).build()
                ).barcode("askdjajskdajksldjaksdjk")
                .state(state).build();
    }

    public Employee createEmployee(EmployeeRole employeeRole) {
        return Employee.builder()
                .name(UUID.randomUUID().toString())
                .employeeRole(employeeRole).build();
    }

    public Instant getTimeAt(int hours, int minutes) {

        return LocalDateTime.of(LocalDate.now(), LocalTime.of(hours, minutes)).toInstant(ZoneOffset.UTC);
    }

    public Instant getDayAt(int day, int month, int year) {
        return LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
