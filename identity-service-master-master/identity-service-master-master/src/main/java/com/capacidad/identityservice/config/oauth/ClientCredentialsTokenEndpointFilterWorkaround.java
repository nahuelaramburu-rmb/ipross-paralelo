package com.capacidad.identityservice.config.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.web.FilterChainProxy;

import javax.servlet.Filter;
import java.util.Optional;

@Configuration
public class ClientCredentialsTokenEndpointFilterWorkaround {

    private final Filter filterChain;

    @Autowired
    public ClientCredentialsTokenEndpointFilterWorkaround(Filter springSecurityFilterChain, ExceptionTranslatorConfig translatorConfig) {
        this.filterChain = springSecurityFilterChain;
        workaround(translatorConfig);
    }

    private void workaround(ExceptionTranslatorConfig translatorConfig) {
        FilterChainProxy filterChainProxy = (FilterChainProxy) filterChain;
        Optional<ClientCredentialsTokenEndpointFilter> optClientTokenEndpointFilter = getValidatedFilter(filterChainProxy);
        optClientTokenEndpointFilter.ifPresent(clientCredentialsTokenEndpointFilter ->
                clientCredentialsTokenEndpointFilter.setAuthenticationEntryPoint(new CustomOAuth2AuthenticationEntryPoint(translatorConfig)));
    }

    private Optional<ClientCredentialsTokenEndpointFilter> getValidatedFilter(FilterChainProxy filterChainProxy) {
        if (filterChainProxy.getFilterChains().size() >= 4) {
            var securityFilterChain = filterChainProxy.getFilterChains().get(4);
            if (securityFilterChain.getFilters().size() >= 4) {
                var filter = securityFilterChain.getFilters().get(4);
                if (filter instanceof ClientCredentialsTokenEndpointFilter)
                    return Optional.ofNullable((ClientCredentialsTokenEndpointFilter) filter);
            }
        }
        return Optional.empty();
    }

}
