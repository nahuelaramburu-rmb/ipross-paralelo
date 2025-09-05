package com.capacidad.validationapi.module.nomenclator.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPracticeArea;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface MedicalPracticeAreaRepository extends ExtendedJpaRepository<MedicalPracticeArea, Long> {
}
