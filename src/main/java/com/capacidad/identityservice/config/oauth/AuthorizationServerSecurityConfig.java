package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerSecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * 🔐 Filtro principal del Authorization Server
     */
    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Configuración base del Authorization Server
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        return http
                .formLogin(Customizer.withDefaults()) // soporte login con formulario
                .build();
    }

    /**
     * 🔑 Codificador de contraseñas para los clientes y usuarios
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 🔧 AuthenticationManager para validar usuarios
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }

    // ⚠️ Aquí no necesitas más configurar clients, tokens o exceptionTranslator
    // porque eso ya se hace en AuthorizationServerConfig (la clase que te pasé antes).
    // Allí defines RegisteredClientRepository, OAuth2AuthorizationService, etc.
}
