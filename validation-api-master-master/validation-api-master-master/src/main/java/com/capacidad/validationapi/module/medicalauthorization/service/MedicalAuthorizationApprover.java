package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;

public abstract class MedicalAuthorizationApprover {

    public final void approve(MedicalAuthorization medicalAuthorization) {
        applyMedicalAuthorizationRules(medicalAuthorization);
        medicalAuthorization.getMedicalAuthorizationItems().forEach(throwingConsumer(this::processMedicalAuthorizationItem));
        determineAuthorizationCondition(medicalAuthorization);
        medicalAuthorization.setStatus(resolveStatus(medicalAuthorization));
    }

    private void processMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException, ObjectNotValidException {
        applyMedicalAuthorizationItemRules(medicalAuthorizationItem);
        if (medicalAuthorizationItem.getBatchItem() != null)
            applyBatchCoverage(medicalAuthorizationItem);
        else {
            applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
            applyContractAdjustments(medicalAuthorizationItem);
            verifyAlreadyPending(medicalAuthorizationItem);
        }
    }

    protected abstract void applyBatchCoverage(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract void applyMedicalAuthorizationRules(MedicalAuthorization medicalAuthorization);

    protected abstract void applyMedicalAuthorizationItemRules(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract void applyMedicalCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    protected abstract void applyContractAdjustments(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract void verifyAlreadyPending(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException;

    protected abstract void determineAuthorizationCondition(MedicalAuthorization medicalAuthorization);

    protected abstract Status resolveStatus(MedicalAuthorization medicalAuthorization);

}
