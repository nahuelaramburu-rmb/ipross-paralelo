package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.PermissionGroup;
import com.capacidad.identityservice.model.PermissionSuggestion;
import com.capacidad.identityservice.model.projection.PermissionGroupProjection;
import com.capacidad.identityservice.model.projection.PermissionSuggestionProjection;
import com.capacidad.utils.exception.ObjectNotFoundException;

import java.util.List;
import java.util.Set;

public interface PermissionGroupService {

    List<PermissionGroupProjection> getAllPermissionGroups();

    List<PermissionGroupProjection> findAllPermissionGroupsByRole(String role);

    List<PermissionSuggestionProjection> findAllPermissionSuggestionsByRole(String role);

    Set<PermissionGroup> findAllBasedOnContextAttributes(ApplicationUserContext context);

    void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext) throws ObjectNotFoundException;

    void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) throws ObjectNotFoundException;

}
