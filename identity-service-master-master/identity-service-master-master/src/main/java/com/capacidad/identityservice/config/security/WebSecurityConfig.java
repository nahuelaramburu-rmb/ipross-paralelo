package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.config.filter.ClientBasicAuthenticationFilter;
import com.capacidad.identityservice.config.filter.I18nFilter;
import com.capacidad.identityservice.config.filter.JWTAuthenticationFilter;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ADMIN;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.FUNDER;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ALL_PARAMS = "/**";
    private static final String ALL_REGEX_RESULT = ".*";
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final ClientBasicAuthenticationFilter clientBasicAuthenticationFilter;
    private final I18nFilter i18nFilter;

    @Autowired
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
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }


    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.GET, ENDPOINT_HEALTH)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, ENDPOINT_VERIFICATION, ALL_PARAMS))
                .antMatchers(StringUtils.join(ENDPOINT_USERS, ENDPOINT_FORGOT, ALL_PARAMS))
                .regexMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ENDPOINT_PASSWORD, ALL_REGEX_RESULT, "username", ALL_REGEX_RESULT));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, ENDPOINT_ME)).authenticated()
                .antMatchers(StringUtils.join(ENDPOINT_ACTUATOR, ALL_PARAMS)).hasRole(ADMIN)
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ENDPOINT_PASSWORD_RESET)).hasAnyRole(ADMIN, FUNDER)
                .regexMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_USERS, ALL_REGEX_RESULT, "sub", ALL_REGEX_RESULT)).hasAuthority(Utils.buildScope(UPDATE, USERS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_USERS, ALL_PARAMS)).hasAuthority(Utils.buildScope(CREATE, USERS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_USERS, ALL_PARAMS)).hasAuthority(Utils.buildScope(READ, USERS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_USERS, ALL_PARAMS)).hasAuthority(Utils.buildScope(DELETE, USERS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PERMISSION_GROUPS, ALL_PARAMS)).hasAuthority(Utils.buildScope(READ, PERMISSION_GROUPS))
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(i18nFilter, JWTAuthenticationFilter.class)
                .addFilterAfter(clientBasicAuthenticationFilter, JWTAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
