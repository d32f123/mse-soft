package com.itmo.mse.soft.service;

import com.itmo.mse.soft.TableCleaner;
import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.Order;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import com.itmo.mse.soft.repository.EmployeeRepository;
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
    TableCleaner tableCleaner;

    Body createBody() {
        return Body.builder()
                .barcode(UUID.randomUUID().toString())
                .state(BodyState.GROOMED)
                .payment(
                        Payment.builder().order(
                                Order.builder()
                                        .paymentAmount(new BigDecimal("335.50"))
                                        .pickupInstant(Instant.now()).build()
                        ).bitcoinAddress("345959ajsdjk_39312").creationInstant(Instant.now()).build()
                ).build();
    }

    @Test
    void contextLoads() {}

    @Test
    void findByBarcode() {
        var body = createBody();

        bodyService.save(body);

        var loadedBody = bodyService.getBodyByBarcode(body.getBarcode()).orElseThrow();
        assertThat(loadedBody).isEqualToIgnoringGivenFields(body, "payment");
    }

    @Test
    void findByPaymentId() {
        var body = createBody();

        bodyService.save(body);

        var loadedBody = bodyService.getBodyByPaymentId(body.getPayment().getPaymentId()).orElseThrow();
        assertThat(loadedBody).isEqualToIgnoringGivenFields(body, "payment");
    }

    @Test
    void shouldReturnValidTransition() {
        assertThat(bodyService.getValidTransitions(EmployeeRole.GROOMER, BodyState.RECEIVED)).isNotEmpty();
    }

    @Test
    void shouldReturnEmptySetForInvalidState() {
        assertThat(bodyService.getValidTransitions(EmployeeRole.PIG_MASTER, BodyState.AWAITING_RECEIVAL)).isEmpty();
    }

    @Test
    void shouldTransitionBody() {
        var body = createBody();

        bodyService.save(body);

        bodyService.transitionBody(body, EmployeeRole.PIG_MASTER, BodyState.IN_FEEDING);
        assertThat(body.getState()).isEqualByComparingTo(BodyState.IN_FEEDING);
    }

    @Test
    void shouldNotTransitionBody() {
        var body = createBody();
        var initialState = body.getState();

        bodyService.save(body);

        bodyService.transitionBody(body, EmployeeRole.GROOMER, BodyState.IN_GROOMING);
        assertThat(body.getState()).isEqualByComparingTo(initialState);
    }

    @Test
    void shouldNotCreateBodyWhenNoEmployeesAvailable() {
        tableCleaner.clearTables();
        var employee = Employee.builder()
                .name("Vasyan")
                .employeeRole(EmployeeRole.GROOMER)
                .build();
        employeeRepository.save(employee);

        var payment = Payment.builder()
                .order(
                        Order.builder()
                        .paymentAmount(new BigDecimal("123.50"))
                        .pickupInstant(Instant.now())
                        .build()
                ).bitcoinAddress("asdf")
                .creationInstant(Instant.now())
                .build();
        var anotherPayment = Payment.builder()
                .order(
                        Order.builder()
                        .paymentAmount(new BigDecimal("12344.50"))
                        .pickupInstant(Instant.now().plus(Duration.ofMinutes(5)))
                        .build()
                ).bitcoinAddress("fdfkfd")
                .creationInstant(Instant.now())
                .build();

        var body = bodyService.createBody(payment);
        assertThat(body).isNotNull();

        var anotherBody = bodyService.createBody(anotherPayment);
        assertThat(anotherBody).isNull();
    }

}
