package com.capacidad.validationapi.module.insuranceplan.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlanType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface InsurancePlanTypeRepository extends ExtendedJpaRepository<InsurancePlanType, Long> {
}
