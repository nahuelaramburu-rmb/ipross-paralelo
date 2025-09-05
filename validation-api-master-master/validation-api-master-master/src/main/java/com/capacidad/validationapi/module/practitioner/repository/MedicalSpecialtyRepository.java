package com.capacidad.validationapi.module.practitioner.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.projection.MedicalSpecialtyProjection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@TenantFilter(active = false)
@Repository
public interface MedicalSpecialtyRepository extends ExtendedJpaRepository<MedicalSpecialty, Long> {

    List<IdAndNameOnlyProjection> findAllByMedicalSpecialtyTypeId(Long medicalSpecialtyTypeId);

    Set<IdAndNameOnlyProjection> findAllProjectedByPractitionersId(Long practitionerId);

    Set<MedicalSpecialty> findAllByMedicalPracticesId(Long medicalPracticeId);

    Set<MedicalSpecialtyProjection.Full> findAllProjectedByNameContainingIgnoreCase(String name);

}
