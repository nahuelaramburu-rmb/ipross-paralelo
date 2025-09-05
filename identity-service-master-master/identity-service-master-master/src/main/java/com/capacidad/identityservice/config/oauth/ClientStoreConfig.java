package com.capacidad.identityservice.config.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.sql.DataSource;

@Configuration
public class ClientStoreConfig {

    @Bean("jdbcClientDetailsService")
    public JdbcClientDetailsService jdbcClientDetailsService(DataSource dataSource,
                                                             PasswordEncoder passwordEncoder) {
        var jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        jdbcClientDetailsService.setPasswordEncoder(passwordEncoder);
        return jdbcClientDetailsService;
    }

}
