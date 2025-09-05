package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;

import java.io.ByteArrayOutputStream;

public interface MedicalAuthorizationSupportService {

    void discountChargesAndValues(MedicalAuthorization medicalAuthorization);

    void rollBackPreMedicalAuthorization(MedicalAuthorization medicalAuthorization);

    ByteArrayOutputStream buildReceipt(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException;

    void publishStatusUpdateEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization);

    void publishDiagnosisUpdateEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization);

    void publishNewMessageEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization);

}
