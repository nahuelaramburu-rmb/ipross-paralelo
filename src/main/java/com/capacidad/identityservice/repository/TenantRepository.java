package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends ExtendedRepository<Tenant, Long> {

    Optional<Tenant> findByTenantIdAndDeletedIsFalse(UUID tenantId);

    Optional<Tenant> findByName(String name);

}
