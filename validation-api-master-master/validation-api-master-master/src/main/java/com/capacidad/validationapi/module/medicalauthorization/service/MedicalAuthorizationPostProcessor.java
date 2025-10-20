package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;

public abstract class MedicalAuthorizationPostProcessor {

    public final void postProcess(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        determinePreMedicalAuthorization(medicalAuthorization);
        audit(medicalAuthorization);
        calculateMedicalAuthorizationChargeTotal(medicalAuthorization);
        calculateBudget(medicalAuthorization);
        settle(medicalAuthorization);
        saveDigitalSignature(medicalAuthorization);
        savePrescriptions(medicalAuthorization);
    }

    protected abstract void determinePreMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    protected abstract void saveDigitalSignature(MedicalAuthorization medicalAuthorization);

    protected abstract void audit(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException;

    protected abstract void calculateMedicalAuthorizationChargeTotal(MedicalAuthorization medicalAuthorization);

    protected abstract void calculateBudget(MedicalAuthorization medicalAuthorization);

    protected abstract void settle(MedicalAuthorization medicalAuthorization);

    protected abstract void savePrescriptions(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    public abstract void notify(MedicalAuthorization medicalAuthorization);

}
