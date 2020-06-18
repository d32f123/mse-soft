package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.entity.EmployeeRole;
import com.itmo.mse.soft.order.Payment;
import com.itmo.mse.soft.repository.BodyRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BodyService extends EntityService<Body> {

    @Autowired
    protected BodyRepository entityRepository;

    @Override
    protected CrudRepository<Body, UUID> getEntityRepository() {
        return entityRepository;
    }

    @Data
    @Builder
    private static class TransitionKey {
        private EmployeeRole employeeRole;
        private BodyState bodyState;
    }

    private final Map<TransitionKey, Set<BodyState>> stateTransitions = new HashMap<>();

    public BodyService() {
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.GROOMER)
                        .bodyState(BodyState.AWAITING_RECEIVAL).build(),
                Set.of(BodyState.IN_RECEIVAL)
        );
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.GROOMER)
                        .bodyState(BodyState.IN_RECEIVAL).build(),
                Set.of(BodyState.RECEIVED)
        );
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.GROOMER)
                        .bodyState(BodyState.RECEIVED).build(),
                Set.of(BodyState.IN_GROOMING)
        );
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.GROOMER)
                        .bodyState(BodyState.IN_GROOMING).build(),
                Set.of(BodyState.GROOMED)
        );
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.PIG_MASTER)
                        .bodyState(BodyState.GROOMED).build(),
                Set.of(BodyState.IN_FEEDING)
        );
        stateTransitions.put(
                TransitionKey.builder()
                        .employeeRole(EmployeeRole.PIG_MASTER)
                        .bodyState(BodyState.IN_FEEDING).build(),
                Set.of(BodyState.FED)
        );
    }

    public Optional<Body> getBodyByBarcode(String barcode) {
        return entityRepository.findBodyByBarcode(barcode);
    }

    public Optional<Body> getBodyByPaymentId(UUID paymentId) {
        return entityRepository.findBodyByPayment(Payment.builder().paymentId(paymentId).build());
    }

    public Set<BodyState> getValidTransitions(EmployeeRole employeeRole, BodyState bodyState) {
        return this.stateTransitions.getOrDefault(TransitionKey.builder()
                .employeeRole(employeeRole).bodyState(bodyState).build(), Collections.emptySet());
    }

    public Body transitionBody(UUID bodyId, EmployeeRole employeeRole, BodyState desiredState) {
        var body = entityRepository.findById(bodyId).orElseThrow();
        return transitionBody(body, employeeRole, desiredState);
    }
    public Body transitionBody(Body body, EmployeeRole employeeRole, BodyState desiredState) {
        log.debug("Transitioning body '{}' by '{}' to '{}'", body.getId(), employeeRole, desiredState);

        if (!this.getValidTransitions(employeeRole, body.getState()).contains(desiredState)) {
            log.warn("Tried to transition body '{}' by employee '{}' to state '{}' but no valid state found",
                    body.getId(), employeeRole, desiredState);
            return body;
        }

        body.setState(desiredState);
        return this.save(body);
    }

}
