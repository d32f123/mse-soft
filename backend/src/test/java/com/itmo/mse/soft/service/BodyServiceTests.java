package com.itmo.mse.soft.service;

import com.itmo.mse.soft.TableCleaner;
import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.*;
import com.itmo.mse.soft.order.BodyOrder;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.PigstyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BodyServiceTests {

    @Autowired
    BodyService bodyService;

    @Autowired
    BodyRepository bodyRepository;

    @Autowired
    TestHelper testHelper;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PigstyRepository pigstyRepository;

    @Autowired
    TableCleaner tableCleaner;

    Body createBody() {
        return Body.builder()
                .barcode(UUID.randomUUID().toString())
                .state(BodyState.GROOMED)
                .payment(
                        Payment.builder().bodyOrder(
                                BodyOrder.builder()
                                        .paymentAmount(new BigDecimal("335.50"))
                                        .pickupInstant(Instant.now()).build()
                        ).bitcoinAddress("345959ajsdjk_39312").creationInstant(Instant.now()).build()
                ).build();
    }

    @Test
    void contextLoads() {}

    @Test
    void findByBarcode() {
        Body body = createBody();

        bodyService.save(body);

        Body loadedBody = bodyService.getBodyByBarcode(body.getBarcode()).orElseThrow();
        assertThat(loadedBody).isEqualToIgnoringGivenFields(body, "payment");
    }

    @Test
    void findByPaymentId() {
        Body body = createBody();

        bodyService.save(body);

        Body loadedBody = bodyService.getBodyByPaymentId(body.getPayment().getPaymentId()).orElseThrow();
        assertThat(loadedBody).isEqualToIgnoringGivenFields(body, "payment");
    }

    @Test
    void shouldNotCreateBodyWhenNoEmployeesAvailable() {
        tableCleaner.clearTables();
        Employee employee = Employee.builder()
                .name("Vasyan")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        Employee anotherEmployee = Employee.builder()
                .name("groomer")
                .employeeRole(EmployeeRole.PIG_MASTER)
                .build();
        employeeRepository.save(employee);
        employeeRepository.save(anotherEmployee);
        Pigsty pigsty = Pigsty.builder()
                .pigstyNumber(0)
                .pigAmount(2)
                .build();
        pigstyRepository.save(pigsty);

        Payment payment = Payment.builder()
                .bodyOrder(
                        BodyOrder.builder()
                        .paymentAmount(new BigDecimal("123.50"))
                        .pickupInstant(Instant.now())
                        .build()
                ).bitcoinAddress("asdf")
                .creationInstant(Instant.now())
                .build();
        Payment anotherPayment = Payment.builder()
                .bodyOrder(
                        BodyOrder.builder()
                        .paymentAmount(new BigDecimal("12344.50"))
                        .pickupInstant(Instant.now().plus(Duration.ofMinutes(5)))
                        .build()
                ).bitcoinAddress("fdfkfd")
                .creationInstant(Instant.now())
                .build();

        Body body = bodyService.createBody(payment);
        assertThat(body).isNotNull();

        Body anotherBody = bodyService.createBody(anotherPayment);
        assertThat(anotherBody).isNull();
    }

}
