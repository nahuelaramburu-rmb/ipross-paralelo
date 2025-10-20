package com.capacidad.validationapi.module.budget.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface BeneficiaryBudgetRepository extends ExtendedJpaRepository<BeneficiaryBudget, Long> {

    Optional<BeneficiaryBudget> findByBeneficiaryIdAndCompanyIdAndStatusId(Long beneficiaryId, Long companyId, Long statusId);

    boolean existsByIdAndBeneficiaryResourceId(Long id, UUID resourceId);

    @TenantFilter(active = false)
    Set<BeneficiaryBudget> findAllByStatusIdAndClosedAtIsNull(long statusId);

}
