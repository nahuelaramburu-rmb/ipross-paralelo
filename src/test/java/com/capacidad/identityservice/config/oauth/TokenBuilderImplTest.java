package com.capacidad.identityservice.config.oauth;

//package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.config.token.TokenBuilderImpl;
import com.capacidad.identityservice.exception.TokenSigningException;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.identityservice.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenBuilderImplTest {

    private ApplicationUserContextService userContextService;
    private TenantService tenantService;
    private PermissionGroupService permissionGroupService;
    private ApplicationProperties applicationProperties;
    private JwtEncoder jwtEncoder;

    private TokenBuilderImpl tokenBuilder;

    @BeforeEach
    void setUp() {
        userContextService = mock(ApplicationUserContextService.class);
        tenantService = mock(TenantService.class);
        permissionGroupService = mock(PermissionGroupService.class);
        applicationProperties = mock(ApplicationProperties.class);
        jwtEncoder = mock(JwtEncoder.class);

        when(applicationProperties.getJwtIssuer()).thenReturn("issuer");
        when(applicationProperties.getActiveProfile()).thenReturn("test");

        tokenBuilder = new TokenBuilderImpl(
                userContextService,
                tenantService,
                permissionGroupService,
                applicationProperties,
                jwtEncoder
        );
    }

    @Test
    void testBuildAccessToken_UserAuthentication() {
        // Mocks
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        ApplicationUser user = new ApplicationUser();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setEmailVerified(true);
        userDetails.setApplicationUser(user);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getName()).thenReturn("testUser");

        RegisteredClient registeredClient = mock(RegisteredClient.class);
        when(registeredClient.getClientId()).thenReturn("clientId");
        when(registeredClient.getScopes()).thenReturn(Set.of("read", "write"));
        when(registeredClient.getTokenSettings()).thenReturn(mock(RegisteredClient.TokenSettings.class));
        when(registeredClient.getTokenSettings().getAccessTokenTimeToLive()).thenReturn(Duration.ofHours(1));

        ApplicationUserContext context = new ApplicationUserContext();
        context.setRole(new Role("ADMIN"));
        context.setTenant(new Tenant(1L, "TenantName"));
        when(tenantService.validateTenant(user, registeredClient, null)).thenReturn(context);
        when(permissionGroupService.findAllBasedOnContextAttributes(context)).thenReturn(Collections.emptySet());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("tokenValue");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Ejecución
        String token = tokenBuilder.buildAccessToken(authentication, registeredClient);

        // Verificación
        assertNotNull(token);
        assertEquals("tokenValue", token);
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testBuildAccessToken_ClientOnly() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        when(authentication.getName()).thenReturn("client");

        RegisteredClient registeredClient = mock(RegisteredClient.class);
        when(registeredClient.getClientId()).thenReturn("clientId");
        when(registeredClient.getScopes()).thenReturn(Set.of("read"));
        when(registeredClient.getTokenSettings()).thenReturn(mock(RegisteredClient.TokenSettings.class));
        when(registeredClient.getTokenSettings().getAccessTokenTimeToLive()).thenReturn(Duration.ofHours(1));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("clientToken");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenBuilder.buildAccessToken(authentication, registeredClient);

        assertNotNull(token);
        assertEquals("clientToken", token);
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testBuildAccessToken_ThrowsTokenSigningException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        when(authentication.getName()).thenReturn("client");

        RegisteredClient registeredClient = mock(RegisteredClient.class);
        when(registeredClient.getClientId()).thenReturn("clientId");
        when(registeredClient.getScopes()).thenReturn(Set.of("read"));
        when(registeredClient.getTokenSettings()).thenReturn(mock(RegisteredClient.TokenSettings.class));
        when(registeredClient.getTokenSettings().getAccessTokenTimeToLive()).thenReturn(Duration.ofHours(1));

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenThrow(new RuntimeException("error"));

        assertThrows(TokenSigningException.class,
                () -> tokenBuilder.buildAccessToken(authentication, registeredClient));
    }
}

