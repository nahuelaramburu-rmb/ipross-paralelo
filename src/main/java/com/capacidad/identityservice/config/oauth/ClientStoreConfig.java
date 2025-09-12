package com.capacidad.identityservice.config.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import javax.sql.DataSource;

@Configuration
public class ClientStoreConfig {

    /**
     * Repositorio de clientes en JDBC
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource,
                                                                 PasswordEncoder passwordEncoder) {
        JdbcRegisteredClientRepository jdbcRepository = new JdbcRegisteredClientRepository((JdbcOperations) dataSource);

        // Nota: La codificación de passwords se aplica directamente al crear los RegisteredClients
        // ejemplo: clientSecret("{noop}miSecreto")
        return jdbcRepository;
    }
}
