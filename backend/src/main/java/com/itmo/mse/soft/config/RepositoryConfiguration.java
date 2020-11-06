package com.itmo.mse.soft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    public RepositoryConfiguration(){
        super();
    }

    @Bean
    OrderEventHandler orderRepositoryEventListener(){
        return new OrderEventHandler();
    }
}
