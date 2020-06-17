package com.itmo.mse.soft;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.BodyState;
import com.itmo.mse.soft.repository.BodyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RepositoryTests {

    @Autowired
    private BodyRepository bodyRepository;

    @Test
    void savesBody() {
        Body body = Body.builder().state(BodyState.IN_GROOMING).barcode("somecode").build();

        bodyRepository.save(body);
        assertThat(body.getId()).isNotNull();
    }

    @Test
    void loadsBody() {
        Body body = Body.builder().state(BodyState.IN_FEEDING).barcode("somecode").paymentId(UUID.randomUUID()).build();

        bodyRepository.save(body);

        Body savedBody = bodyRepository.findById(body.getId()).orElseThrow();
        assertThat(savedBody).isEqualToComparingFieldByField(body);
    }

}
