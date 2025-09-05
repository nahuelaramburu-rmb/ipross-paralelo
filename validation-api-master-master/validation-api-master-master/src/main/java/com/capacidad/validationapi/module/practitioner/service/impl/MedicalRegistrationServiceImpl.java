package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.practitioner.dto.MedicalRegistrationDTO;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.MedicalRegistrationProjection;
import com.capacidad.validationapi.module.practitioner.repository.MedicalRegistrationRepository;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Log4j2
@Service
public class MedicalRegistrationServiceImpl extends BaseServiceImpl<MedicalRegistration, MedicalRegistrationDTO, Long> implements MedicalRegistrationService {

    private final MedicalRegistrationRepository medicalRegistrationRepository;

    @Autowired
    public MedicalRegistrationServiceImpl(MedicalRegistrationRepository repository) {
        super(repository);
        this.medicalRegistrationRepository = repository;
    }

    @Override
    public MedicalRegistration create(MedicalRegistrationDTO dto, Practitioner practitioner) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        MedicalRegistration object = this.mapDtoToInput(dto);
        object.setPractitioner(practitioner);
        this.validate(object);
        MedicalRegistration objectResult = medicalRegistrationRepository.save(object);
        log.info("create - void: {}({})", object.getClass(), object);
        return objectResult;
    }

    @Override
    public boolean practitionerBelongsToAuthOrganization(long practitionerId) {
        return medicalRegistrationRepository
                .existsByPractitionerIdAndOrganizationResourceId(
                        practitionerId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)
                );
    }

    @Override
    public boolean practitionerBelongsOrganization(long practitionerId, long organizationId) {
        return medicalRegistrationRepository
                .existsByPractitionerIdAndOrganizationId(
                        practitionerId, organizationId
                );
    }

    @Override
    public Set<MedicalRegistrationProjection> getMedicalRegistrations(long practitionerId) {
        return medicalRegistrationRepository
                .findAllByPractitionerId(practitionerId);
    }

}
