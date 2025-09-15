package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CustomTokenServiceTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private RedisOAuth2AuthorizationService authorizationService;

    @InjectMocks
    private CustomTokenService customTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtEncodingContext jwtEncodingContext;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear usuario de prueba
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        customUserDetails = new CustomUserDetails(user, "tenant123");

        // Mock principal
        when(jwtEncodingContext.getPrincipal()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testCustomize_withJwtClaimsSetBuilder() {
        // Crear un builder real de JwtClaimsSet
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("testuser");

        // Mock para devolver nuestro builder
        when(jwtEncodingContext.getClaims()).thenReturn(claimsBuilder);

        // Mockear servicio
        when(customUserDetailsService.loadUserByUsername("testuser"))
                .thenReturn(customUserDetails);

        // Ejecutar customize
        customTokenService.customize(jwtEncodingContext);

        // Verificar los claims agregados
        JwtClaimsSet finalClaims = claimsBuilder.build();
        assertEquals(1L, (Long) finalClaims.getClaim("user_id"));
        assertEquals("test@example.com", finalClaims.getClaim("email"));
        assertEquals("tenant123", finalClaims.getClaim("tenant_id"));

        verify(customUserDetailsService, times(1)).loadUserByUsername("testuser");
    }

    @Test
    void testFindAuthorization() {
        OAuth2Authorization auth = mock(OAuth2Authorization.class);
        when(authorizationService.findByToken("token123", null)).thenReturn(auth);

        OAuth2Authorization result = customTokenService.findAuthorization("token123");

        assertEquals(auth, result);
        verify(authorizationService, times(1)).findByToken("token123", null);
    }

    @Test
    void testRevokeAuthorization_WhenAuthExists() {
        OAuth2Authorization auth = mock(OAuth2Authorization.class);
        when(authorizationService.findByToken("token123", null)).thenReturn(auth);

        customTokenService.revokeAuthorization("token123");

        verify(authorizationService, times(1)).remove(auth);
    }

    @Test
    void testRevokeAuthorization_WhenAuthDoesNotExist() {
        when(authorizationService.findByToken("token123", null)).thenReturn(null);

        customTokenService.revokeAuthorization("token123");

        verify(authorizationService, never()).remove(any());
    }
}
