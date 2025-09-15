package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.PermissionGroup;
import com.capacidad.identityservice.model.PermissionSuggestion;
import com.capacidad.identityservice.repository.PermissionGroupRepository;
import com.capacidad.identityservice.repository.PermissionSuggestionRepository;
import com.capacidad.utils.exception.ObjectNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_ADMIN_AUTHORITY;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_FUNDER_AUTHORITY;
import static com.capacidad.identityservice.model.PermissionStrategy.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionGroupServiceImplTest {

    @Mock
    private SecurityContext securityContext;
    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;
    @Mock
    private PermissionSuggestionRepository permissionSuggestionRepository;
    @Mock
    private PermissionGroupRepository permissionGroupRepository;
    @InjectMocks
    private PermissionGroupServiceImpl permissionGroupService;

    @Test
    public void testSetPermissionsAndStrategyToContextSetDefaultRoleWhenNotAdminAndNullStrategy() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_FUNDER_AUTHORITY));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(DEFAULT_ROLE);

        SecurityContextHolder.setContext(currentContext);
    }

    @Test
    public void testSetPermissionsAndStrategyToContextSetDefaultRoleWhenAdminAndNullGroups() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_AUTHORITY));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(DEFAULT_ROLE);

        SecurityContextHolder.setContext(currentContext);
    }

    @Test
    public void testSetPermissionsAndStrategyToContextSetDefaultRoleWhenAdminAndEmptyGroups() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setPermissionGroups(new ArrayList<>());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_AUTHORITY));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(DEFAULT_ROLE);

        SecurityContextHolder.setContext(currentContext);
    }

    @Test
    public void testSetPermissionsAndStrategyToContextSetDefaultRoleWhenAdminAndBothStrategiesSpecified() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setPermissionGroups(Collections.singletonList(new PermissionGroup()));
        userContext.setPermissionSuggestion(new PermissionSuggestion());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_AUTHORITY));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(DEFAULT_ROLE);

        SecurityContextHolder.setContext(currentContext);
    }

    @Test
    public void testSetPermissionsAndStrategyToContextSetSuggestionWhenAdminValidData() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setPermissionGroups(null);

        PermissionSuggestion permissionSuggestion = new PermissionSuggestion();
        permissionSuggestion.setId(1L);
        userContext.setPermissionSuggestion(permissionSuggestion);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_AUTHORITY));
        when(permissionSuggestionRepository.findById(permissionSuggestion.getId())).thenReturn(Optional.of(permissionSuggestion));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(PERMISSION_SUGGESTION);
        assertThat(userContext.getPermissionSuggestion()).isNotNull();
        assertThat(userContext.getPermissionGroups().size()).isZero();

        SecurityContextHolder.setContext(currentContext);
    }

    @Test
    public void testSetPermissionsAndStrategyToContextSetGroupsWhenAdminValidData() throws ObjectNotFoundException {
        SecurityContext currentContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        PermissionGroup permissionGroup = new PermissionGroup();
        permissionGroup.setId(1L);
        userContext.setPermissionGroups(Collections.singletonList(permissionGroup));
        userContext.setPermissionSuggestion(null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_AUTHORITY));
        when(permissionGroupRepository.findAllByIdIn(anySet())).thenReturn(Collections.singletonList(permissionGroup));

        permissionGroupService.setPermissionsAndStrategyToContext(userContext);

        assertThat(userContext.getPermissionStrategy()).isEqualTo(PERMISSION_GROUPS);
        assertThat(userContext.getPermissionSuggestion()).isNull();
        assertThat(userContext.getPermissionGroups().size()).isNotZero();

        SecurityContextHolder.setContext(currentContext);
    }

}
