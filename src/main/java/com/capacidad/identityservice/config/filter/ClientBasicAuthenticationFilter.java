package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.ClientAuthenticationToken;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.utils.SecurityUtils;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.Utils.isNotCustomEndpoint;
import static com.capacidad.identityservice.misc.Utils.sendError;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_CLIENT_AUTHORITY;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;
import static com.capacidad.utils.Constants.JWT_CLAIM_GROUP;

@Component
public class ClientBasicAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalExceptionHandler exceptionHandler;
    private final ClientDetailsService clientDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TenantService tenantService;

    @Autowired
    public ClientBasicAuthenticationFilter(GlobalExceptionHandler exceptionHandler,
                                           ClientDetailsService clientDetailsService,
                                           PasswordEncoder passwordEncoder,
                                           TenantService tenantService) {
        this.exceptionHandler = exceptionHandler;
        this.clientDetailsService = clientDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tenantService = tenantService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Optional<String[]> basicAuthCredentials = SecurityUtils.getBasicAuthorizationCredentials(request);
        if (basicAuthCredentials.isEmpty() || isNotCustomEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Authentication authentication = getAuthentication(basicAuthCredentials.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (ObjectNotFoundException e) {
            sendError(response, exceptionHandler.handleObjectNotFoundException(request, e));
        } catch (AuthenticationException e) {
            sendError(response, exceptionHandler.handleAuthenticationException(request, e));
        } finally {
            TenantContext.clearContext();
            SecurityContextHolder.clearContext();
        }
    }

    private Authentication getAuthentication(String[] basicAuthCredentials) throws ObjectNotFoundException {
        try {
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(basicAuthCredentials[0]);
            setTenantContext(clientDetails);
            if (!passwordEncoder.matches(basicAuthCredentials[1], clientDetails.getClientSecret()))
                throw new BadCredentialsException("Invalid Client Credentials");
            Optional<String> groupString = Utils.getClientAdditionalInformation(JWT_CLAIM_GROUP, clientDetails);
            Group clientGroup = groupString.map(gp -> Group.valueOf(gp.toUpperCase())).orElse(null);
            return new ClientAuthenticationToken(clientDetails.getClientId(),
                    "",
                    getClientAuthorities(clientDetails),
                    clientGroup);
        } catch (NoSuchClientException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    private Collection<? extends GrantedAuthority> getClientAuthorities(ClientDetails clientDetails) {
        List<GrantedAuthority> authorities = clientDetails.getScope().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        authorities.add(ROLE_CLIENT_AUTHORITY);
        return authorities;
    }

    private void setTenantContext(ClientDetails clientDetails) throws ObjectNotFoundException {
        String tenantId = Utils.getClientAdditionalInformation(TENANT_ID, clientDetails).orElse("");
        if (StringUtils.isNotBlank(tenantId)) {
            Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));
            TenantContext.setTenant(tenant);
        }
    }

}
