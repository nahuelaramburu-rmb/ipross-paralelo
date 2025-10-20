package com.capacidad.validationapi.module.procedure.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.procedure.model.CertificateType;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface CertificateTypeRepository extends ExtendedJpaRepository<CertificateType, Long> {
}
