package com.capacidad.validationapi.module.budget.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@TenantFilter
public interface PractitionerBudgetRepository extends ExtendedJpaRepository<PractitionerBudget, Long> {

    Optional<PractitionerBudget> findByPractitionerIdAndMedicalCenterIdAndStatusId(Long practitionerId, Long medicalCenterId, Long statusId);

    boolean existsByIdAndPractitionerResourceId(Long budgetId, UUID practitionerResourceId);

    boolean existsByIdAndMedicalCenterResourceId(Long budgetId, UUID medicalCenterResourceId);

}
