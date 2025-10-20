package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.prescription.model.Prescription;

import java.io.ByteArrayOutputStream;

public interface PrescriptionSupportService {

    Period getPrescriptionExpirationPeriod();

    String buildPrescriptionKey(Prescription prescription) throws ObjectNotValidException;

    void sendNewPrescriptionNotification(Prescription prescription);

    ByteArrayOutputStream generatePrescriptionReceipt(Prescription prescription) throws ObjectNotValidException;

}
