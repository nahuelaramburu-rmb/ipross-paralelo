package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.projection.ApplicationUserContextView;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ApplicationUserContextRepository extends ExtendedRepository<ApplicationUserContext, Long> {

    Optional<ApplicationUserContext> findByUserUsernameAndTenantTenantId(String username, UUID tenantId);

    Set<ApplicationUserContext> findAllByUserResourceIdAndTenantTenantId(UUID resourceId, UUID tenantId);

    Set<ApplicationUserContextView> findAllByUserUsernameOrUserEmail(String username, String email);




}
