package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.model.ScopeRole;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
import com.capacidad.identityservice.repository.ScopeRoleRepository;
import com.capacidad.identityservice.service.ScopeRoleService;
import com.capacidad.identityservice.service.base.BaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;


// obtiene los alcances de operaciones sobre la data de cada user , de acuerdo a su rol ,

@Service
@Slf4j
public class ScopeRoleServiceImpl extends BaseServiceImpl<ScopeRole, Long> implements ScopeRoleService {

    private final ScopeRoleRepository scopeRoleRepository;

    @Autowired
    public ScopeRoleServiceImpl(ScopeRoleRepository repository) {
        super(repository);
        this.scopeRoleRepository = repository;
    }

    @Deprecated
    @Override
    public Set<ScopeRole> findRoleScopeGroup(Role role, Tenant tenant) {
        Set<ScopeRoleViewDTO> scopeRoleViews = scopeRoleRepository
                .findAllByRoleNameAndTenantTenantId(role.getName(), tenant.getTenantId());
        if (scopeRoleViews.isEmpty())
            return buildFromView(scopeRoleRepository
                    .findAllByRoleNameAndTenantIsNull(role.getName()));
        return buildFromView(scopeRoleViews);
    }

    private Set<ScopeRole> buildFromView(Set<ScopeRoleViewDTO> scopeRoleViews) {
        return scopeRoleViews.stream()
                .map(ScopeRoleViewDTO::getScopeRole)
                .collect(Collectors.toSet());
    }

}
