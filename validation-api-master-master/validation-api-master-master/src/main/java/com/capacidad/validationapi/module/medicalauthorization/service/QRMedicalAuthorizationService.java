package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.dto.QRMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.QRMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;

public interface QRMedicalAuthorizationService extends BaseMedicalAuthorizationService<QRMedicalAuthorization, QRMedicalAuthorizationDTO> {

    MedicalAuthorizationProjection.QR resolveQrCode(String qrCode) throws ObjectNotValidException, ObjectNotFoundException;

}
