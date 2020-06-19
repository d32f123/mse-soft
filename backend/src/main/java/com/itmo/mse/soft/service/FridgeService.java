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
        return body;
    }

    public Body closeFridge(Body body) {
        // TODO: Implement autocomplete of task so that body automatically transitions to new state
        return body;
    }

}
