package com.capacidad.validationapi.module.medicalcenter.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.projection.MedicalCenterProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface MedicalCenterRepository extends ExtendedJpaRepository<MedicalCenter, Long> {

    Set<MedicalCenterProjection.IdNameAndAddressProjection> findAllProjectedByPractitionersId(Long practitionerId);

    boolean existsByResourceIdAndPractitionersId(UUID resourceId, Long practitionerId);

    Set<MedicalCenterProjection> findProjectedByNameContainingIgnoreCase(String name);

    Optional<MedicalCenter> findByResourceId(UUID resourceId);

    Optional<MedicalCenterProjection> findProjectedByResourceId(UUID resourceId);

}