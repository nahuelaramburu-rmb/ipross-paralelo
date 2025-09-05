package com.capacidad.validationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableAspectJAutoProxy
@EnableConfigurationProperties
public class ApiCapacidadApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiCapacidadApplication.class, args);
    }
}
