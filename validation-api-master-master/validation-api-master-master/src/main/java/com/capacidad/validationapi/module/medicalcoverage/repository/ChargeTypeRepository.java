package com.capacidad.validationapi.module.medicalcoverage.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.medicalcoverage.model.ChargeType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface ChargeTypeRepository extends ExtendedJpaRepository<ChargeType, Long> {
}
