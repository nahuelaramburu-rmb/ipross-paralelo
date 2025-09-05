package com.capacidad.validationapi.module.properties.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.projection.PropertiesProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@TenantFilter(active = false)
@Repository
public interface PropertiesRepository extends ExtendedJpaRepository<Properties, Long> {

    Optional<PropertiesProjection> findOptionallyByTenantIdIsNull();

    Optional<PropertiesProjection> findOptionallyByTenantId(UUID tenantId);

    Optional<Properties> findByTenantIdIsNull();

    Optional<Properties> findByTenantId(UUID tenantId);

}
