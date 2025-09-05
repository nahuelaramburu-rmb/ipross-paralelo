package com.capacidad.validationapi.module.insuranceplan.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import org.springframework.stereotype.Repository;

@TenantFilter
@Repository
public interface InsurancePlanRepository extends ExtendedJpaRepository<InsurancePlan, Long> {
}
