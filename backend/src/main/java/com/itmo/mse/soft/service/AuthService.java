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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AuthService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public String authenticate(String employeeName) {
        return authenticate(employeeName, employeeName);
    }
    public String authenticate(String employeeName, String password) {
        var employee = employeeRepository.findByNameAndPassword(employeeName, password).orElse(null);
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

    public void logout(String tokenBase){
        var tokenId = new String(Base64.getUrlDecoder().decode(tokenBase), StandardCharsets.UTF_8);
        var token = tokenRepository.findById(UUID.fromString(tokenId)).orElse(null);
        if (token != null)
            tokenRepository.deleteById(UUID.fromString(tokenId));
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
