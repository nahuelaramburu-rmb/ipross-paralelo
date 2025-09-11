package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.config.token.TokenBuilderImpl;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class OAuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBuilderImpl tokenBuilder;
    private final JdbcClientDetailsService jdbcClientDetailsService;
    private final TokenStore tokenStore;
    private final WebResponseExceptionTranslator<OAuth2Exception> exceptionTranslator;

    @Autowired
    public OAuthServerConfig(AuthenticationManager authenticationManager,
                             CustomUserDetailsService userDetailsService,
                             TokenBuilderImpl tokenBuilder,
                             TokenStore tokenStore,
                             @Qualifier("jdbcClientDetailsService") JdbcClientDetailsService jdbcClientDetailsService,
                             WebResponseExceptionTranslator<OAuth2Exception> exceptionTranslator) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenBuilder = tokenBuilder;
        this.tokenStore = tokenStore;
        this.jdbcClientDetailsService = jdbcClientDetailsService;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public void configure(
            AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer
                .allowFormAuthenticationForClients()
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(jdbcClientDetailsService);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .pathMapping("/oauth/check_token", "/oauth/verify_token")
                .tokenServices(tokenServices())
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager)
                .exceptionTranslator(exceptionTranslator);
    }

    @Bean
    public CustomTokenServices tokenServices() {
        return new CustomTokenServices(tokenStore,
                tokenBuilder,
                jdbcClientDetailsService,
                userDetailsService);
    }

    @Bean
    public DefaultOAuth2RequestFactory defaultOAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(jdbcClientDetailsService);
    }

}
