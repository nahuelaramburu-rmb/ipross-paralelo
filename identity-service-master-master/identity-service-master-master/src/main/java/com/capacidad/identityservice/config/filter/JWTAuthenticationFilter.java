package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.config.token.TokenVerifier;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.utils.SecurityUtils;
import com.capacidad.utils.TokenUtils;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.identityservice.misc.Utils.isNotCustomEndpoint;
import static com.capacidad.identityservice.misc.Utils.sendError;
import static com.capacidad.utils.Constants.*;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final TokenVerifier tokenVerifier;
    private final TenantService tenantService;
    private final GlobalExceptionHandler exceptionHandler;

    @Autowired
    public JWTAuthenticationFilter(TenantService tenantService,
                                   TokenVerifier tokenVerifier,
                                   GlobalExceptionHandler exceptionHandler) {
        this.tenantService = tenantService;
        this.tokenVerifier = tokenVerifier;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        Optional<String> bearerToken = SecurityUtils.getBearerToken(request);
        if (bearerToken.isEmpty() || isNotCustomEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Authentication authentication = getAuthentication(bearerToken.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (Exception e) {
            Utils.resolveException(exceptionHandler, request, e)
                    .ifPresent(throwingConsumer(r -> sendError(response, r)));
        } finally {
            TenantContext.clearContext();
            SecurityContextHolder.clearContext();
        }
    }

    private Authentication getAuthentication(String jwt) throws ObjectNotFoundException {
        tokenVerifier.validate(jwt);
        Map<String, Object> jwtValues = TokenUtils.parseJwt(jwt);
        setTenant((String) jwtValues.get(JWT_CLAIM_TENANT));
        String groupString = (String) jwtValues.get(JWT_CLAIM_GROUP);
        Group group = Group.valueOf(groupString.toUpperCase());
        String resourceId = TokenUtils.getJwtResourceIdClaim(jwt).orElse("");
        UUID uuidResourceId = StringUtils.isNotBlank(resourceId) ? UUID.fromString(resourceId) : null;
        return new JWTAuthenticationToken(jwtValues.get(JWT_CLAIM_USERNAME), "", getAuthorities(jwtValues), group, uuidResourceId);
    }

    private void setTenant(String tenantId) throws ObjectNotFoundException {
        Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));
        TenantContext.setTenant(tenant);
    }

    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> getAuthorities(Map<String, Object> jwtValues) {
        List<String> scopeList = (List<String>) jwtValues.get(JWT_CLAIM_SCOPE);
        List<GrantedAuthority> grantedAuthorities = scopeList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        grantedAuthorities.add(new SimpleGrantedAuthority((String) jwtValues.get(JWT_CLAIM_ROLE)));
        return grantedAuthorities;
    }

}
