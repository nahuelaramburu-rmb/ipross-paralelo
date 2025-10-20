package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.PermissionSuggestion;
import com.capacidad.identityservice.model.projection.PermissionSuggestionProjection;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionSuggestionRepository extends ExtendedRepository<PermissionSuggestion, Long> {

    List<PermissionSuggestionProjection> findAllProjectedByRoleName(String roleName);

}
