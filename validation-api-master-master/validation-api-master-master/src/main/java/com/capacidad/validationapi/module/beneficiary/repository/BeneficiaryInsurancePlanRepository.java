package com.capacidad.validationapi.module.beneficiary.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryInsurancePlanProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@TenantFilter
@Repository
public interface BeneficiaryInsurancePlanRepository extends ExtendedJpaRepository<BeneficiaryInsurancePlan, Long> {

    @TenantFilter(active = false)
    Set<BeneficiaryInsurancePlan> findAllByExpirationDateNotNullAndExpirationDateLessThan(LocalDate now);

    Set<BeneficiaryInsurancePlanProjection> findAllProjectedByBeneficiaryId(Long beneficiaryId);

    Optional<BeneficiaryInsurancePlanProjection.BeneficiaryId> findBeneficiaryIdProjectionById(long beneficiaryInsurancePlanId);

    List<BeneficiaryInsurancePlan> findAllByInsurancePlanId(long insurancePlanId);

}
