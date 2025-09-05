package com.capacidad.validationapi.module.prescription.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.prescription.model.PrescriptionRestriction;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@TenantFilter
public interface PrescriptionRestrictionRepository extends ExtendedJpaRepository<PrescriptionRestriction, Long> {

    int countAllByMedicalSpecialtyIn(Collection<MedicalSpecialty> medicalSpecialty);

}