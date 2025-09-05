package com.capacidad.validationapi.module.medicalcenter.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.medicalcenter.dto.MedicalCenterDTO;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.projection.MedicalCenterProjection;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterRepository;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
public class MedicalCenterServiceImpl extends BaseServiceImpl<MedicalCenter, MedicalCenterDTO, Long> implements MedicalCenterService {

    private final MedicalCenterRepository medicalCenterRepository;
    private final IdentityClientService identityClientService;

    @Autowired
    public MedicalCenterServiceImpl(MedicalCenterRepository repository,
                                    IdentityClientService identityClientService) {
        super(repository);
        this.medicalCenterRepository = repository;
        this.identityClientService = identityClientService;
    }

    @Override
    public void validate(MedicalCenter object) {
        object.setResourceId(UUID.randomUUID());
    }

    @Override
    public Set<MedicalCenterProjection.IdNameAndAddressProjection> getPractitionerMedicalCenters(long practitionerId) {
        return medicalCenterRepository.findAllProjectedByPractitionersId(practitionerId);
    }

    @Override
    public boolean authMedicalCenterContainsPractitioner(long practitionerId) {
        return medicalCenterRepository
                .existsByResourceIdAndPractitionersId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null), practitionerId);
    }

    @Override
    public Set<MedicalCenterProjection> getMedicalCenters(String name) {
        return medicalCenterRepository
                .findProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public MedicalCenter getAuthMedicalCenter() throws ObjectNotFoundException {
        return medicalCenterRepository
                .findByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException(
                        "medicalCenter.resourceIdNotFound")
                );
    }

    @Override
    public MedicalCenterProjection getProjectedAuthMedicalCenter() throws ObjectNotFoundException {
        return medicalCenterRepository
                .findProjectedByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException(
                        "medicalCenter.resourceIdNotFound")
                );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long medicalCenterId) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalCenter medicalCenter = this.findById(medicalCenterId);
        if (medicalCenter.getContract() != null && !medicalCenter.getContract().getDeleted())
            throw new ObjectNotValidException("contractShouldBeDeletedFirst", medicalCenter.getContract().getName());
        UUID deletionToken = UUID.randomUUID();
        medicalCenter.getAddress().setDeleted(true);
        medicalCenter.getAddress().setDeletionToken(deletionToken);
        medicalCenter.setDeleted(true);
        medicalCenter.setDeletionToken(deletionToken);
        medicalCenter.getPractitioners().forEach(p -> p.getMedicalCenters().remove(medicalCenter));
        medicalCenter.getBatchItems().forEach(i -> i.getMedicalCenters().remove(medicalCenter));
        medicalCenterRepository.save(medicalCenter);
        identityClientService.deleteResourceIdAccounts(medicalCenter.getResourceId());
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(medicalCenter, medicalCenterRepository));
        return this.buildIdResponse(medicalCenter.getId());
    }

}
