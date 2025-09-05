package com.capacidad.validationapi.prescription.galbop.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;

public interface GalbopPrescriptionValidator {

    void validate(GalbopPrescription galbopPrescription, Prescription prescription) throws ObjectNotFoundException, ObjectNotValidException;

}
