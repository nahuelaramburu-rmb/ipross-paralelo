package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface RestrictionTypeRepository extends ExtendedJpaRepository<RestrictionType, Long> {
}
