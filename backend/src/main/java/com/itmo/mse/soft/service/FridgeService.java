package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Body;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FridgeService {

    @Autowired
    private BodyService bodyService;

    public Body enterFridge(Body body) {
        log.info("BODY IS IN THE FRIDGE");
        return body;
    }

    public Body closeFridge(Body body) {
        log.info("BODY IS OUT OF THE FRIDGE");
        return body;
    }

}
