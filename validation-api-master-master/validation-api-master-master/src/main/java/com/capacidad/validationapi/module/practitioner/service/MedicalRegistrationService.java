package com.capacidad.validationapi.module.practitioner.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.practitioner.dto.MedicalRegistrationDTO;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.MedicalRegistrationProjection;

import java.util.Set;

public interface MedicalRegistrationService extends BaseService<MedicalRegistration, MedicalRegistrationDTO, Long> {

    Set<MedicalRegistrationProjection> getMedicalRegistrations(long practitionerId);

    MedicalRegistration create(MedicalRegistrationDTO dto, Practitioner practitioner) throws ObjectNotValidException, ObjectNotFoundException;

    boolean practitionerBelongsToAuthOrganization(long practitionerId);

    boolean practitionerBelongsOrganization(long practitionerId, long organizationId);

}
