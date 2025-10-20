package com.capacidad.validationapi.module.storage.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import com.capacidad.validationapi.module.storage.service.StorageServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Component
public class StorageServiceValidatorImpl implements StorageServiceValidator {

    private final MedicalAuthorizationService medicalAuthorizationService;

    @Autowired
    public StorageServiceValidatorImpl(MedicalAuthorizationService medicalAuthorizationService) {
        this.medicalAuthorizationService = medicalAuthorizationService;
    }

    @Override
    public void validateAttachment(long authorizationId) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationService.findById(authorizationId);
        Long statusId = medicalAuthorization.getStatus().getId();
        if (!statusId.equals(VALIDATION_PENDING.getId()))
            throw new ObjectNotValidException(String.format(
                    "MedicalAuthorization id: %d should be PENDING", authorizationId
            ));
    }

    @Override
    public void validateReport(long authorizationId) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationService.findById(authorizationId);
        Long statusId = medicalAuthorization.getStatus().getId();
        if (!statusId.equals(VALIDATION_APPROVED.getId()) && !statusId.equals(VALIDATION_PARTIALLY_APPROVED.getId()))
            throw new ObjectNotValidException(String.format(
                    "MedicalAuthorization id: %d should be APPROVED or PARTIALLY APPROVED", authorizationId
            ));
        medicalAuthorization.getMedicalAuthorizationItems().forEach(throwingConsumer(item -> {
            if (Boolean.TRUE.equals(item.getSettled()))
                throw new ObjectNotValidException(String.format(
                        "MedicalAuthorization with id: %d contains Settled items", medicalAuthorization.getId()
                ));
        }));
    }

}
