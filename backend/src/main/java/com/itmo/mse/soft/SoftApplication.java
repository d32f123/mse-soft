package com.itmo.mse.soft;

import org.hibernate.boot.MetadataBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SoftApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftApplication.class, args);
    }

}
