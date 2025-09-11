package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.exception.TokenSigningException;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.identityservice.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;

@Slf4j
@Component
public class TokenBuilderImpl {

    private final ApplicationUserContextService userContextService;
    private final TenantService tenantService;
    private final PermissionGroupService permissionGroupService;
    private final ApplicationProperties applicationProperties;
    private final JwtEncoder jwtEncoder;

    public TokenBuilderImpl(ApplicationUserContextService userContextService,
                            TenantService tenantService,
                            PermissionGroupService permissionGroupService,
                            ApplicationProperties applicationProperties,
                            JwtEncoder jwtEncoder) {
        this.userContextService = userContextService;
        this.tenantService = tenantService;
        this.permissionGroupService = permissionGroupService;
        this.applicationProperties = applicationProperties;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Construye un JWT Access Token con claims personalizados
     * para usuarios y client-only.
     */
    public String buildAccessToken(Authentication authentication, RegisteredClient registeredClient) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(
                registeredClient.getTokenSettings().getAccessTokenTimeToLive().getSeconds()
        );

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(applicationProperties.getJwtIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .claim("client_id", registeredClient.getClientId())
                .claim("scope", String.join(" ", registeredClient.getScopes()));

        // Caso usuario autenticado
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            ApplicationUser user = userDetails.getApplicationUser();

            ApplicationUserContext context = tenantService.validateTenant(user, registeredClient,
                    userDetails.getTenantId());

            userContextService.checkOperationalState(user, context, registeredClient);

            Set<PermissionGroup> permissionGroups = permissionGroupService.findAllBasedOnContextAttributes(context);

            String newScope = permissionGroups.stream()
                    .flatMap(p -> p.getResourceOperations().entrySet().stream()
                            .flatMap(e -> e.getValue().stream()
                                    .map(op -> op.toString().toLowerCase() + ":" + e.getKey())))
                    .collect(Collectors.joining(COMA));

            Tenant tenant = context.getTenant();

            claimsBuilder
                    .claim("username", user.getUsername())
                    .claim("role", context.getRole().getName().toLowerCase())
                    .claim("tenant", Map.of(
                            "id", tenant.getId(),
                            "name", tenant.getName(),
                            "role", context.getRole().getName()
                    ))
                    .claim("group", applicationProperties.getActiveProfile().toLowerCase())
                    .claim("scope", newScope)
                    .claim("email", user.getEmail())
                    .claim("email_verified", user.getEmailVerified())
                    .claim("resource_id", user.getResourceId() != null ? user.getResourceId().toString() : "")
                    .claim("subrole", context.getPermissionSuggestion() != null ? context.getPermissionSuggestion().getId() : null)
                    .claim("aud", registeredClient.getClientId());
        }
        // Caso client-only
        else {
            claimsBuilder
                    .claim("username", "client")
                    .claim("role", "client")
                    .claim("group", applicationProperties.getActiveProfile().toLowerCase())
                    .claim("tenant", Map.of("client_id", registeredClient.getClientId()))
                    .claim("aud", registeredClient.getClientId());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        try {
            return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            log.error("Error al firmar JWT: {}", e.getMessage());
            throw new TokenSigningException("tokenBuilder.signingError");
        }
    }
}
