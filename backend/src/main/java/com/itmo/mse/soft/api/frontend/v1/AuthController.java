package com.itmo.mse.soft.api.frontend.v1;


import com.itmo.mse.soft.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping
   public ResponseEntity<String> authenticate(
        @RequestBody AuthData authData) {
        var token = authService.authenticate(authData.getUserName(), authData.getPassword());
        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(token);
    }

}
