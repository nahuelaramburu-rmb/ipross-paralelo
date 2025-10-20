package com.capacidad.validationapi.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class AmqpConfig {

    private final ConnectionFactory connectionFactory;

    @Autowired
    public AmqpConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    Connection rabbitMqConnection() {
        return connectionFactory.createConnection();
    }

}
