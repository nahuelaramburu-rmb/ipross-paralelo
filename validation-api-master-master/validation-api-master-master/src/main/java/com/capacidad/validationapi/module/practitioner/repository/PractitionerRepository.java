package com.capacidad.validationapi.module.practitioner.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.person.repository.BasePersonRepository;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface PractitionerRepository extends BasePersonRepository<Practitioner, Long> {

    Set<PractitionerProjection.Minor> findAllProjectedByLastNameContainingIgnoreCaseAndStatusId(String lastName, Long statusId);

    Optional<PersonDetailProjection> findPractitionerProjectedById(Long practitionerId);

    boolean existsByIdAndMedicalCentersId(Long practitionerId, Long medicalCenterId);

    boolean existsByIdAndResourceId(Long practitionerId, UUID resourceId);

    Optional<PractitionerProjection> findProjectedByResourceId(UUID resourceId);

    Optional<Practitioner> findByResourceId(UUID resourceId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Practitioner> findLockedByResourceId(UUID resourceId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Practitioner> findLockedById(long practitionerId);

    Optional<BaseProjection<Long>> findByMedicalRegistrationsId(long medicalRegistrationId);

    @TenantFilter(active = false)
    Set<Practitioner> findAllBy();

    Optional<Practitioner> findByIdTypeIdAndIdNumber(long idTypeId, long idNumber);

}
