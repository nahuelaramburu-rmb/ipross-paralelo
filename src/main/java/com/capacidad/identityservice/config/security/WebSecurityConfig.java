package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.config.filter.ClientBasicAuthenticationFilter;
import com.capacidad.identityservice.config.filter.I18nFilter;
import com.capacidad.identityservice.config.filter.JWTAuthenticationFilter;
import com.capacidad.identityservice.config.oauth.CustomAuthEntryPoint;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ADMIN;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.FUNDER;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final ClientBasicAuthenticationFilter clientBasicAuthenticationFilter;
    private final I18nFilter i18nFilter;

    public WebSecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter,
                             CustomUserDetailsService userDetailsService,
                             PasswordEncoder passwordEncoder,
                             ClientBasicAuthenticationFilter clientBasicAuthenticationFilter,
                             I18nFilter i18nFilter) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.clientBasicAuthenticationFilter = clientBasicAuthenticationFilter;
        this.i18nFilter = i18nFilter;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthEntryPoint customAuthEntryPoint) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Configuración de endpoints ignorados
        http.csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, ENDPOINT_HEALTH).permitAll()
                        .requestMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, ENDPOINT_VERIFICATION, "/**")).permitAll()
                        .requestMatchers(StringUtils.join(ENDPOINT_USERS, ENDPOINT_FORGOT, "/**")).permitAll()
                        .requestMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ENDPOINT_PASSWORD, ".*", "username", ".*")).permitAll()
                        .requestMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, ENDPOINT_ME)).authenticated()
                        .requestMatchers(StringUtils.join(ENDPOINT_ACTUATOR, "/**")).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ENDPOINT_PASSWORD_RESET)).hasAnyRole(ADMIN, FUNDER)
                        .requestMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ".*", "sub", ".*")).hasAuthority(Utils.buildScope(UPDATE, USERS))
                        .requestMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_USERS, "/**")).hasAuthority(Utils.buildScope(CREATE, USERS))
                        .requestMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, "/**")).hasAuthority(Utils.buildScope(READ, USERS))
                        .requestMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_USERS, "/**")).hasAuthority(Utils.buildScope(DELETE, USERS))
                        .requestMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PERMISSION_GROUPS, "/**")).hasAuthority(Utils.buildScope(READ, PERMISSION_GROUPS))
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint) // manejador custom
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(i18nFilter, JWTAuthenticationFilter.class)
                .addFilterAfter(clientBasicAuthenticationFilter, JWTAuthenticationFilter.class);

        return http.build();
    }

}
