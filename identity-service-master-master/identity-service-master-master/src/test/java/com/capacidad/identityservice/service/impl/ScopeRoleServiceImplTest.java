package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.model.ScopeRole;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
import com.capacidad.identityservice.repository.ScopeRoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScopeRoleServiceImplTest {

    @Mock
    private ScopeRoleRepository scopeRoleRepository;
    @InjectMocks
    private ScopeRoleServiceImpl scopeService;

    @Test
    public void testFindRoleScopeGroupReturnsEmptyWhenNoTenantNorGeneralScopes() {
        Tenant tenant = new Tenant();
        UUID tenantId = UUID.randomUUID();
        tenant.setTenantId(tenantId);
        Role role = new Role();
        role.setName(ADMIN);

        when(scopeRoleRepository
                .findAllByRoleNameAndTenantTenantId(role.getName(), tenantId)).thenReturn(Collections.emptySet());
        when(scopeRoleRepository
                .findAllByRoleNameAndTenantIsNull(role.getName())).thenReturn(Collections.emptySet());

        Set<ScopeRole> result = scopeService.findRoleScopeGroup(role, tenant);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindRoleScopeGroupReturnsValidScopesWhenNoTenantButGeneralScopes() {
        Tenant tenant = new Tenant();
        UUID tenantId = UUID.randomUUID();
        tenant.setTenantId(tenantId);
        Role role = new Role();
        role.setName(ADMIN);

        Set<ScopeRoleViewDTO> scopeRoleSet = new HashSet<>();
        scopeRoleSet.add(new ScopeRoleViewDTO("resource", Collections.emptyList()));

        when(scopeRoleRepository
                .findAllByRoleNameAndTenantTenantId(role.getName(), tenantId)).thenReturn(Collections.emptySet());
        when(scopeRoleRepository
                .findAllByRoleNameAndTenantIsNull(role.getName())).thenReturn(scopeRoleSet);

        Set<ScopeRole> result = scopeService.findRoleScopeGroup(role, tenant);

        assertThat(result).hasSize(scopeRoleSet.size());
    }

    @Test
    public void testFindRoleScopeGroupReturnsValidScopesWhenNoGeneralButTenantScopes() {
        Tenant tenant = new Tenant();
        UUID tenantId = UUID.randomUUID();
        tenant.setTenantId(tenantId);
        Role role = new Role();
        role.setName(ADMIN);

        Set<ScopeRoleViewDTO> scopeRoleSet = new HashSet<>();
        scopeRoleSet.add(new ScopeRoleViewDTO("resource", Collections.emptyList()));

        when(scopeRoleRepository
                .findAllByRoleNameAndTenantTenantId(role.getName(), tenantId)).thenReturn(scopeRoleSet);

        Set<ScopeRole> result = scopeService.findRoleScopeGroup(role, tenant);

        assertThat(result).hasSize(scopeRoleSet.size());
        verify(scopeRoleRepository, never()).findAllByRoleNameAndTenantIsNull(role.getName());
    }

}
