package com.capacidad.validationapi.module.organization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.organization.projection.OrganizationProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface OrganizationRepository extends ExtendedJpaRepository<Organization, Long> {

    Set<OrganizationProjection> findProjectedByNameContainingIgnoreCase(String name);

    Optional<OrganizationProjection> findProjectedByResourceId(UUID resourceId);

    Optional<Organization> findByResourceId(UUID resourceId);

    Set<Organization> findAllByRelatedOrganizationId(long relatedOrganizationId);

}
