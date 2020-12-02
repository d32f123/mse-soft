package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Employee;
import com.itmo.mse.soft.entity.Token;
import com.itmo.mse.soft.repository.EmployeeRepository;
import com.itmo.mse.soft.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public String authenticate(String employeeName) {
        var employee = employeeRepository.findByName(employeeName).orElse(null);
        if (employee == null) {
            return null;
        }

        var token = Token.builder()
                .employee(employee)
                .expirationInstant(Instant.now().plus(Duration.ofMinutes(60)))
                .build();
        tokenRepository.saveAndFlush(token);

        return Base64.getUrlEncoder().encodeToString(token.getId().toString().getBytes(StandardCharsets.UTF_8));
    }

    public Employee getEmployeeByToken(String tokenBase) {
        var tokenId = new String(Base64.getUrlDecoder().decode(tokenBase), StandardCharsets.UTF_8);
        var token = tokenRepository.findById(UUID.fromString(tokenId)).orElse(null);
        if (token == null) {
            return null;
        }

        return token.getEmployee();
    }

}
