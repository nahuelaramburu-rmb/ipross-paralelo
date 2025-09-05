package com.capacidad.validationapi.module.premedicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;

public interface PreMedicalAuthorizationValidator {

    void validate(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException;

    void validateStatus(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException;

    Status determineStatus(PreMedicalAuthorization preMedicalAuthorization);

    void determineItemConsumption(PreMedicalAuthorizationItem preMedicalAuthorizationItem, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException;

}
