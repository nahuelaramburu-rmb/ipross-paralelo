package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.dto.IdMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MagstripeMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.OTPMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.QRMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;

public interface MedicalAuthorizationMediator {

    MedicalAuthorizationProjection apply(IdMedicalAuthorizationDTO idMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException;

    MedicalAuthorizationProjection apply(MagstripeMedicalAuthorizationDTO magstripeMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException;

    MedicalAuthorizationProjection apply(OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException;

    MedicalAuthorizationProjection apply(QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException;

}
