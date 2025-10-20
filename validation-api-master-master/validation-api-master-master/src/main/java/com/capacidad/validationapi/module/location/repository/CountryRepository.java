package com.capacidad.validationapi.module.location.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.location.model.Country;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface CountryRepository extends ExtendedJpaRepository<Country, Long> {
}
