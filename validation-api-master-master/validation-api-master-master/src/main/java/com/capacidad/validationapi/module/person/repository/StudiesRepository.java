package com.capacidad.validationapi.module.person.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.person.model.Studies;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface StudiesRepository extends ExtendedJpaRepository<Studies, Long> {
}
