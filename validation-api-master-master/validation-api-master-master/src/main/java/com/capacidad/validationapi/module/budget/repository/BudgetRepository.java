package com.capacidad.validationapi.module.budget.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.budget.model.Budget;
import org.springframework.stereotype.Repository;

@TenantFilter
@Repository
public interface BudgetRepository extends ExtendedJpaRepository<Budget, Long> {
}
