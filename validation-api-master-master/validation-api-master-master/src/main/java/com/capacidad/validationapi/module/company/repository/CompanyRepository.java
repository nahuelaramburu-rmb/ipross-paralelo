package com.capacidad.validationapi.module.company.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.company.projection.CompanyProjection;
import org.springframework.stereotype.Repository;

import java.util.Set;

@TenantFilter
@Repository
public interface CompanyRepository extends ExtendedJpaRepository<Company, Long> {
    Set<CompanyProjection> findAllProjectedByNameContainingIgnoreCase(String name);
}
