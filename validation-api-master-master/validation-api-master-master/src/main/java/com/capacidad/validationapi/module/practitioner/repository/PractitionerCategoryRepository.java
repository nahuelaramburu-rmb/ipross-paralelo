package com.capacidad.validationapi.module.practitioner.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import org.springframework.stereotype.Repository;

@TenantFilter
@Repository
public interface PractitionerCategoryRepository extends ExtendedJpaRepository<PractitionerCategory, Long> {
}
