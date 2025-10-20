package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.prescription.model.Prescription;

public interface PrescriptionValidator {

    void validate(Prescription prescription) throws ObjectNotValidException, ObjectNotFoundException;

    void validateFromMedicalAuthorization(Prescription prescription) throws ObjectNotValidException;

}
