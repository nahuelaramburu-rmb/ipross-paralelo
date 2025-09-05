package com.capacidad.validationapi.module.practitioner.service;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.practitioner.dto.PractitionerDTO;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.rating.Rating;

import java.util.Optional;
import java.util.Set;

public interface PractitionerService extends BaseService<Practitioner, PractitionerDTO, Long> {

    PractitionerProjection getProjectedAuthPractitioner() throws ObjectNotFoundException;

    Practitioner getAuthPractitioner() throws ObjectNotFoundException;

    boolean existsByAuthentication(long practitionerId);

    boolean belongsToMedicalCenter(Practitioner practitioner, MedicalCenter medicalCenter);

    PersonDetailProjection getPersonDetails(long practitionerId) throws ObjectNotFoundException;

    Set<PractitionerProjection.Minor> findAvailablePractitioners(String lastName);

    boolean canPerformMedicalPractice(Practitioner practitioner, MedicalPractice medicalPractice);

    PractitionerProjection associateMedicalCenter(long practitionerId, long medicalCenterId) throws ObjectNotFoundException, ObjectAlreadyExistsException;

    PractitionerProjection dissociateMedicalCenter(long practitionerId, long medicalCenterId) throws ObjectNotFoundException, ObjectNotValidException;

    PractitionerProjection associateContract(long practitionerId, long contractId) throws ObjectNotFoundException, ObjectAlreadyExistsException;

    PractitionerProjection dissociateContract(long practitionerId, long contractId) throws ObjectNotFoundException, ObjectNotValidException;

    PractitionerProjection associateMedicalSpecialty(long practitionerId, long medicalCenterId) throws ObjectNotFoundException, ObjectAlreadyExistsException;

    PractitionerProjection dissociateMedicalSpecialty(long practitionerId, long medicalCenterId) throws ObjectNotFoundException, ObjectNotValidException;

    BaseProjection<Long> getMedicalRegistrationOwner(long medicalRegistrationId) throws ObjectNotFoundException;

    PractitionerProjection updateStatus(long practitionerId, StatusUpdateDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    void renewAllKeys();

    String getAuthPractitionerKey() throws ObjectNotFoundException;

    Optional<Practitioner> findOptionallyByIdTypeAndIdNumber(IdType idType, Long idNumber);

    void addRating(Practitioner practitioner, Rating rating);

    void removeRating(Practitioner practitioner, Rating rating);

    Practitioner findByIdLocked(long practitionerId) throws ObjectNotFoundException;

    Practitioner getAuthPractitionerLocked() throws ObjectNotFoundException;

}
