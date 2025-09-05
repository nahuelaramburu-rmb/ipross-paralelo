package com.capacidad.identityservice;

import com.capacidad.identityservice.repository.base.JpaSpecificationExecutorWithProjectionImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(
        repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class,
        basePackages = "com.capacidad.identityservice")
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
