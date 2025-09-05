package com.capacidad.validationapi.config.filter;

import com.capacidad.utils.SecurityUtils;
import com.capacidad.utils.TokenUtils;
import com.capacidad.utils.exception.InvalidTokenException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.exception.GlobalExceptionHandler;
import com.capacidad.validationapi.misc.ApplicationProperties;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import static com.capacidad.utils.Constants.*;

@Log4j2
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_CLAIM_CLIENT_ID = "client_id";
    private final GlobalExceptionHandler exceptionHandler;
    private final ApplicationProperties applicationProperties;

    @Autowired
    JWTAuthenticationFilter(GlobalExceptionHandler globalExceptionHandler,
                            ApplicationProperties applicationProperties) {
        this.exceptionHandler = globalExceptionHandler;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
        Optional<String> bearerToken = SecurityUtils.getBearerToken(request);
        if (bearerToken.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String jwt = bearerToken.orElse("");
            Map<String, Object> jwtValues = parseAndValidateJwt(jwt);
            Authentication authentication = getAuthentication(jwt, jwtValues);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            sendError(response, exceptionHandler.handleInvalidTokenException(request, e));
        } finally {
            TenantContext.clearTenant();
            SecurityContextHolder.clearContext();
        }
    }

    public Map<String, Object> parseAndValidateJwt(String jwt) {
        validateAudience(jwt);
        return TokenUtils.parseJwt(jwt);
    }

    private Authentication getAuthentication(String jwt, Map<String, Object> jwtValues) {
        TenantContext.setTenant(UUID.fromString((String) jwtValues.get(JWT_CLAIM_TENANT)));
        String resourceId = TokenUtils.getJwtResourceIdClaim(jwt).orElse("");
        UUID uuidResourceId = StringUtils.isNotBlank(resourceId) ? UUID.fromString(resourceId) : null;
        String subString = TokenUtils.getJwtSubject(jwt).orElse("");
        UUID sub = parseSubToUUID(subString);
        String clientId = TokenUtils.getClaim(jwt, JWT_CLAIM_CLIENT_ID)
                .orElseGet(() -> TokenUtils.getJwtSubject(jwt)
                        .orElse("null_client_id"));
        return new JWTAuthenticationToken((String) jwtValues.get(JWT_CLAIM_USERNAME),
                getAuthorities(jwtValues),
                uuidResourceId,
                clientId,
                sub, jwt);
    }

    private UUID parseSubToUUID(String sub) {
        if (StringUtils.isNotBlank(sub)) {
            try {
                return UUID.fromString(sub);
            } catch (IllegalArgumentException e) {
                log.debug("Cannot convert sub ({}) to UUID: {}", sub, e.getMessage());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> getAuthorities(Map<String, Object> jwtValues) {
        List<String> scopeList = (List<String>) jwtValues.get(JWT_CLAIM_SCOPE);
        List<GrantedAuthority> grantedAuthorities = scopeList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        grantedAuthorities.add(new SimpleGrantedAuthority((String) jwtValues.get(JWT_CLAIM_ROLE)));
        return grantedAuthorities;
    }

    private void sendError(HttpServletResponse response, ResponseEntity<Object> responseEntity) throws IOException {
        String apiError = (String) responseEntity.getBody();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(responseEntity.getStatusCodeValue());
        if (apiError != null)
            response.getWriter().write(apiError);
    }

    private void validateAudience(String jwt) {
        TokenUtils.validate(jwt, applicationProperties.getApiIdentifier(), applicationProperties.getActiveProfile());
    }

}
