package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.PermissionGroup;
import com.capacidad.identityservice.model.projection.PermissionGroupProjection;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionGroupRepository extends ExtendedRepository<PermissionGroup, Long> {

    List<PermissionGroupProjection> findAllProjectedBy();

    List<PermissionGroupProjection> findAllProjectedByRolesName(String roleName);

    List<PermissionGroup> findAllByIdIn(Set<Long> ids);

}
