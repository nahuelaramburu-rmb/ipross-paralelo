package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.model.ScopeRole;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.base.BaseService;

import java.util.Set;

public interface ScopeRoleService extends BaseService<ScopeRole, Long> {

    @Deprecated
    Set<ScopeRole> findRoleScopeGroup(Role role, Tenant tenant);

}
