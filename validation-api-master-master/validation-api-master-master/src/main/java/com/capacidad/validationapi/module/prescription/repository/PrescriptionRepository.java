package com.capacidad.validationapi.module.prescription.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface PrescriptionRepository extends ExtendedJpaRepository<Prescription, Long> {

    boolean existsByIdAndMedicalCenterResourceId(Long id, UUID resourceId);

    boolean existsByIdAndPractitionerResourceId(Long id, UUID resourceId);

    boolean existsByIdAndBeneficiaryResourceId(Long id, UUID resourceId);

    boolean existsByIdAndBeneficiaryFamilyId(Long id, UUID familyId);

    @TenantFilter(active = false)
    Set<Prescription> findAllByStatusId(Long statusId);

}
