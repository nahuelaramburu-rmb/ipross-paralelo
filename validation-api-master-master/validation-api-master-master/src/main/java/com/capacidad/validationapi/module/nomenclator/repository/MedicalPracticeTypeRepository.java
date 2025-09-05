package com.capacidad.validationapi.module.nomenclator.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPracticeType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface MedicalPracticeTypeRepository extends ExtendedJpaRepository<MedicalPracticeType, Long> {
}
