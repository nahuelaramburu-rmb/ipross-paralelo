package com.capacidad.identityservice.config.oauth;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        when(applicationProperties.getJwtIssuer()).thenReturn("test-issuer");
        when(applicationProperties.getActiveProfile()).thenReturn("test-profile");

        tokenBuilder = new TokenBuilderImpl(
                userContextService,
                tenantService,
                permissionGroupService,
                applicationProperties,
                jwtEncoder
        );
    }

    @Test
    void buildAccessToken_withUserAuthentication_shouldIncludeUserClaims() {
        // Mock Authentication
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        ApplicationUser user = new ApplicationUser();
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setEmailVerified(true);
        user.setResourceId(UUID.randomUUID());

        when(auth.getPrincipal()).thenReturn(userDetails);
        when(auth.getName()).thenReturn("john");
        when(userDetails.getApplicationUser()).thenReturn(user);
        when(userDetails.getTenantId()).thenReturn("tenant1");

        // Mock RegisteredClient
        RegisteredClient client = RegisteredClient.withId("1")
                .clientId("client1")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .scopes(scopes -> scopes.add("read"))
                .build();

        // Mock tenant and permissions
        ApplicationUserContext context = new ApplicationUserContext();
        context.setTenant(new Tenant("tenant1", "Tenant One"));
        context.setRole(new Role("ADMIN"));

        PermissionSuggestion suggestion = new PermissionSuggestion();
        suggestion.setId(42L);
        context.setPermissionSuggestion(suggestion);

        when(tenantService.validateTenant(any(), any(), any())).thenReturn(context);
        when(permissionGroupService.findAllBasedOnContextAttributes(any())).thenReturn(Set.of());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("signed-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenBuilder.buildAccessToken(auth, client);

        assertEquals("signed-token", token);

        // Verifica claims
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        Map<String, Object> claims = captor.getValue().getClaims().getClaims();

        assertEquals("john", claims.get("username"));
        assertEquals("john@test.com", claims.get("email"));
        assertEquals("admin", claims.get("role"));
    }

    @Test
    void buildAccessToken_withClientOnly_shouldIncludeClientClaims() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("anonymous");
        when(auth.getName()).thenReturn("client");

        RegisteredClient client = RegisteredClient.withId("1")
                .clientId("client1")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .scopes(scopes -> scopes.add("read"))
                .build();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("client-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenBuilder.buildAccessToken(auth, client);

        assertEquals("client-token", token);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        Map<String, Object> claims = captor.getValue().getClaims().getClaims();

        assertEquals("client", claims.get("username"));
        assertEquals("client", claims.get("role"));
    }

    @Test
    void buildAccessToken_jwtEncoderThrows_shouldThrowTokenSigningException() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("anonymous");
        when(auth.getName()).thenReturn("client");

        RegisteredClient client = RegisteredClient.withId("1")
                .clientId("client1")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .scopes(scopes -> scopes.add("read"))
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenThrow(new RuntimeException("fail"));

        assertThrows(TokenSigningException.class,
                () -> tokenBuilder.buildAccessToken(auth, client));
    }
}
