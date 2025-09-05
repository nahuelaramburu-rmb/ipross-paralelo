package com.capacidad.validationapi.module.practitioner.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.projection.MedicalRegistrationProjection;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface MedicalRegistrationRepository extends ExtendedJpaRepository<MedicalRegistration, Long> {

    Set<MedicalRegistrationProjection> findAllByPractitionerId(Long practitionerId);

    boolean existsByPractitionerIdAndOrganizationResourceId(Long practitionerId, UUID resourceId);

    boolean existsByPractitionerIdAndOrganizationId(Long practitionerId, Long organizationId);

}
