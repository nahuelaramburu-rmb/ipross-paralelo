package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.repository.RoleRepository;
import com.capacidad.identityservice.service.RoleService;
import com.capacidad.identityservice.service.base.BaseServiceImpl;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COLON;
import static com.capacidad.utils.Constants.ROLE_PREFIX;

@Service
public class RoleServiceImpl extends BaseServiceImpl<Role, Long> implements RoleService {

    private static final String TOKEN_PREFIX = "token";
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository repository) {
        super(repository);
        this.roleRepository = repository;
    }

    @Override
    public Role findRole(String name) throws ObjectNotFoundException {
        return roleRepository
                .findByNameIgnoreCase(name)
                .orElseThrow(() -> new ObjectNotFoundException("role.notFoundName", name));
    }

    @Override
    public Role validateAuthorityRoleAccess(Role role, String operation) throws ObjectNotFoundException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> userScopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        String currentRoleName = userScopes
                .stream()
                .filter(authority -> StringUtils.startsWith(authority, ROLE_PREFIX))
                .findFirst()
                .orElse(null);
        var roleToValidate = StringUtils.join(operation, COLON, role.getName().toLowerCase());
        var currentRole = findRole(StringUtils.remove(currentRoleName, ROLE_PREFIX));
        if (currentRole.getAccessLevel() <= role.getAccessLevel() && !currentRole.getId().equals(role.getId())
                && !userScopes.contains(roleToValidate))
            throw new InsufficientAuthenticationException("role.roleAccessPermission");
        return currentRole;
    }

    @Override
    public void validateClientRoleAccess(Role role, Set<String> clientScopes) {
        String roleName = role.getName();
        var scopeToValidate = StringUtils.join(TOKEN_PREFIX, COLON, roleName.toLowerCase());
        var allTokenPermission = StringUtils.join(TOKEN_PREFIX, COLON, "all");
        if (!clientScopes.contains(scopeToValidate) && !clientScopes.contains(allTokenPermission))
            throw new InsufficientAuthenticationException("role.roleClientTokenAccessPermission");
    }

    @Override
    public List<Role> findRoles(List<String> roles) {
        List<String> uppercaseRoles = roles.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toUnmodifiableList());
        return roleRepository.findAllByNameIn(uppercaseRoles);
    }
}
