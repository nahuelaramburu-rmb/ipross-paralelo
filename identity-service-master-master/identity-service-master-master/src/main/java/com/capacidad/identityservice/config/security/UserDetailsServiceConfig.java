package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService(ApplicationUserContextService userContextService) {
        return new CustomUserDetailsService(userContextService);
    }

}
