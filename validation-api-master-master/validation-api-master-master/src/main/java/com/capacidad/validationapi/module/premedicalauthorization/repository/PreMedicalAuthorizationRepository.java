package com.capacidad.validationapi.module.premedicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@TenantFilter
@Repository
public interface PreMedicalAuthorizationRepository extends ExtendedJpaRepository<PreMedicalAuthorization, Long> {

    Optional<PreMedicalAuthorization> findByCode(String preMedicalAuthorizationCode);

    boolean existsByIdAndBeneficiaryResourceId(long preMedicalAuthorizationId, UUID resourceId);

    boolean existsByIdAndBeneficiaryFamilyId(long preMedicalAuthorizationId, UUID familyId);

    boolean existsByCodeAndBeneficiaryResourceId(String code, UUID resourceId);

    boolean existsByCodeAndBeneficiaryFamilyId(String code, UUID familyId);

}
