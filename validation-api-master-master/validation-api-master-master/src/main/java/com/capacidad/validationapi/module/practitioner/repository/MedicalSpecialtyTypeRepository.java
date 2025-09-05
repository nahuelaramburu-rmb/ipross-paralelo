package com.capacidad.validationapi.module.practitioner.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialtyType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface MedicalSpecialtyTypeRepository extends ExtendedJpaRepository<MedicalSpecialtyType, Long> {
}
