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
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ADMIN;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.FUNDER;


// por ahora se implementa login sin JWTAuthenticationFilter y ClientBasicAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
// habilita la seguridad a nivel de métodos con anotaciones como @PreAuthorize y @PostAuthorize.
public class WebSecurityConfig {


    //filtro que valida tokens JWT en cada request
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    // filtro extra para autenticar clientes vía Basic Auth , oauth2
    // private final ClientBasicAuthenticationFilter clientBasicAuthenticationFilter;

    //filtro que gestiona la internacionalización (idiomas) en las peticiones
    private final I18nFilter i18nFilter;

    public WebSecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter,
                             //   ClientBasicAuthenticationFilter clientBasicAuthenticationFilter,
                             I18nFilter i18nFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        //  this.clientBasicAuthenticationFilter = clientBasicAuthenticationFilter;
        this.i18nFilter = i18nFilter;
    }


    // se define toda la configuración (endpoints protegidos, filtros, roles, etc.).
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthEntryPoint customAuthEntryPoint) throws Exception {

        //aplica configuración por defecto para un Authorization Server OAuth2 (endpoints de autorización, tokens, etc.).
        //OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Configuración de endpoints ignorados
        http
                .cors()
                .and()
                .csrf().disable()

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.GET, ENDPOINT_HEALTH).permitAll()

                        // swagger
                        .requestMatchers(HttpMethod.GET,
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html"
                        ).permitAll()


                        .requestMatchers(HttpMethod.GET, ENDPOINT_USERS + ENDPOINT_VERIFICATION + "/**").permitAll()

                        .requestMatchers(ENDPOINT_USERS + ENDPOINT_FORGOT + "/**").permitAll()

                        .requestMatchers(HttpMethod.PUT, ENDPOINT_USERS + ENDPOINT_PASSWORD + "/**/username/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ENDPOINT_USERS + ENDPOINT_ME).authenticated()
                        .requestMatchers(ENDPOINT_ACTUATOR + "/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, ENDPOINT_USERS + ENDPOINT_PASSWORD_RESET).hasAnyRole(ADMIN, FUNDER)
                        .requestMatchers(HttpMethod.PUT, ENDPOINT_USERS + "/**/sub/**").hasAuthority(Utils.buildScope(UPDATE, USERS))
                        .requestMatchers(HttpMethod.POST, ENDPOINT_USERS + "/**").hasAuthority(Utils.buildScope(CREATE, USERS))

                        .requestMatchers(HttpMethod.GET, ENDPOINT_USERS + "/**").permitAll() // acceso
                        .requestMatchers(HttpMethod.POST, ENDPOINT_USERS + "/**").permitAll() // acceso

                        .requestMatchers(HttpMethod.POST, ENDPOINT_AUTH + ENDPOINT_LOGIN).permitAll() // acceso a login
                        .requestMatchers(HttpMethod.POST, ENDPOINT_AUTH + ENDPOINT_REGISTER_BENEFICIARY).permitAll() // acceso a register
                        .requestMatchers(HttpMethod.POST, ENDPOINT_AUTH + ENDPOINT_REGISTER_PRACTITIONER).permitAll() // acceso a register


                        .requestMatchers(HttpMethod.DELETE, ENDPOINT_USERS + "/**").hasAuthority(Utils.buildScope(DELETE, USERS))
                        .requestMatchers(HttpMethod.DELETE, ENDPOINT_USERS + "/**").hasAuthority(Utils.buildScope(DELETE, USERS))


                        .anyRequest().authenticated()
                )
                //.authenticationProvider(authenticationProvider)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint) // manejar errores de autenticación personalizados.
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class);
        // .addFilterBefore(i18nFilter, JWTAuthenticationFilter.class);
        //  .addFilterAfter(clientBasicAuthenticationFilter, JWTAuthenticationFilter.class);  // TODO , REVISAR clientBasicAuthenticationFilter

        return http.build();
    }

}
