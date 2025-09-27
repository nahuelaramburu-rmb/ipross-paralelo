package com.capacidad.identityservice.config.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
public class AuthorizationServerConfig {

//    @Bean
//    @Order(1)
//    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
//        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
//        return http.build();
//    }
//
//    @Bean
//    public AuthorizationServerSettings authorizationServerSettings() {
//        return AuthorizationServerSettings.builder().build();
//    }


}
