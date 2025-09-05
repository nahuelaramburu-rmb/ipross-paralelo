package com.capacidad.validationapi.module.beneficiary.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import org.springframework.stereotype.Repository;

@TenantFilter
@Repository
public interface BeneficiaryCategoryRepository extends ExtendedJpaRepository<BeneficiaryCategory, Long> {
}
