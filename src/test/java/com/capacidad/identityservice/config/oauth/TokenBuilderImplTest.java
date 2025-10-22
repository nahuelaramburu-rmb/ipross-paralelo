//package com.capacidad.identityservice.config.oauth;
//
//import com.capacidad.identityservice.config.token.TokenBuilderImpl;
//import com.capacidad.identityservice.exception.TokenSigningException;
//import com.capacidad.identityservice.misc.ApplicationProperties;
//import com.capacidad.identityservice.model.*;
//import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
//import com.capacidad.identityservice.repository.ScopeRoleRepository;
//import com.capacidad.identityservice.service.ApplicationUserContextService;
//import com.capacidad.identityservice.service.PermissionGroupService;
//import com.capacidad.identityservice.service.TenantService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentMatchers;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.jwt.JwtEncoder;
//import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.mockito.Mockito;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TokenBuilderImplTest {
//
//    private ApplicationUserContextService userContextService;
//    private TenantService tenantService;
//    private PermissionGroupService permissionGroupService;
//    private ApplicationProperties applicationProperties;
//    private JwtEncoder jwtEncoder;
//    private ScopeRoleRepository scopeRoleRepository;
//
//    private TokenBuilderImpl tokenBuilder;
//
//    @BeforeEach
//    void setUp() {
//        userContextService = mock(ApplicationUserContextService.class);
//        tenantService = mock(TenantService.class);
//        permissionGroupService = mock(PermissionGroupService.class);
//        applicationProperties = mock(ApplicationProperties.class);
//        jwtEncoder = mock(JwtEncoder.class);
//        scopeRoleRepository = mock(ScopeRoleRepository.class);
//
//        when(applicationProperties.getJwtIssuer()).thenReturn("test-issuer");
//        when(applicationProperties.getActiveProfile()).thenReturn("test-profile");
//
//        tokenBuilder = new TokenBuilderImpl(
//                userContextService,
//                tenantService,
//                permissionGroupService,
//                applicationProperties,
//                jwtEncoder,
//                scopeRoleRepository
//        );
//    }
//
//    @Test
//    void buildAccessToken_withUser_returnsSignedToken() {
//        // Mock Authentication y usuario
//        CustomUserDetails userDetails = mock(CustomUserDetails.class);
//        ApplicationUser user = new ApplicationUser();
//        user.setUsername("testuser");
//        user.setEmail("test@example.com");
//        user.setEmailVerified(true);
//        user.setContextSet(Set.of(new ApplicationUserContext(new Role("ADMIN"))));
//        when(userDetails.getApplicationUser()).thenReturn(user);
//        when(userDetails.getTenantId()).thenReturn(1L);
//
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//        when(authentication.getName()).thenReturn("testuser");
//
//        // Mock ScopeRoleRepository
//        ScopeRoleViewDTO dto = mock(ScopeRoleViewDTO.class);
//        ScopeRole scopeRole = new ScopeRole();
//        Resource resource = new Resource();
//        resource.setName("invoice");
//        scopeRole.setResource(resource);
//        scopeRole.setOperations(List.of(Operation.READ, Operation.UPDATE));
//
//        when(dto.getScopeRole()).thenReturn(scopeRole);
//        when(scopeRoleRepository.findAllByRoleNameAndTenantIsNull("ADMIN"))
//                .thenReturn(Set.of(dto));
//
//        // Mock JwtEncoder
//        Jwt jwt = mock(Jwt.class);
//        when(jwt.getTokenValue()).thenReturn("signed-token");
//        when(jwtEncoder.encode(ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(jwt);
//
//        String token = tokenBuilder.buildAccessToken(authentication);
//        assertEquals("signed-token", token);
//
//        verify(jwtEncoder, times(1)).encode(ArgumentMatchers.any(JwtEncoderParameters.class));
//    }
//
//    @Test
//    void buildAccessToken_clientOnly_returnsSignedToken() {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn("client-principal");
//        when(authentication.getName()).thenReturn("client");
//
//        Jwt jwt = mock(Jwt.class);
//        when(jwt.getTokenValue()).thenReturn("client-token");
//        when(jwtEncoder.encode(ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(jwt);
//
//        String token = tokenBuilder.buildAccessToken(authentication);
//        assertEquals("client-token", token);
//    }
//
//    @Test
//    void buildAccessToken_whenEncoderFails_throwsTokenSigningException() {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn("client-principal");
//        when(authentication.getName()).thenReturn("client");
//
//        when(jwtEncoder.encode(ArgumentMatchers.any(JwtEncoderParameters.class)))
//                .thenThrow(new RuntimeException("Encoder error"));
//
//        assertThrows(TokenSigningException.class, () -> tokenBuilder.buildAccessToken(authentication));
//    }
//}
