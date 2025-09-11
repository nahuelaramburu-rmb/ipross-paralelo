package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.service.base.BaseService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.List;
import java.util.Set;

public interface RoleService extends BaseService<Role, Long> {

    Role findRole(String name) throws ObjectNotFoundException;

    Role validateAuthorityRoleAccess(Role role, String operation) throws InsufficientAuthenticationException, ObjectNotFoundException;

    void validateClientRoleAccess(Role role, Set<String> clientScopes) throws InsufficientAuthenticationException;

    List<Role> findRoles(List<String> roles);

}
