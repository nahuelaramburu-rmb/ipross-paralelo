package com.capacidad.validationapi.module.budget.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import org.springframework.stereotype.Repository;

import java.util.Set;

@TenantFilter
@Repository
public interface BudgetItemRepository extends ExtendedJpaRepository<BudgetItem, Long> {

    Set<BudgetItem> findAllByMedicalAuthorizationId(Long medicalAuthorizationId);

}
