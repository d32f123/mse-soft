package com.itmo.mse.soft.api.frontend.v1;

import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.service.BodyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/order-status")
public class CustomerController {

    @Autowired
    BodyService bodyService;

    @GetMapping
    public ResponseEntity<BodyState> getBodyState(@RequestParam("paymentId") UUID paymentId) {
        var body = bodyService.getBodyByPaymentId(paymentId).orElse(null);
        if (body == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(body.getState());
    }


}
