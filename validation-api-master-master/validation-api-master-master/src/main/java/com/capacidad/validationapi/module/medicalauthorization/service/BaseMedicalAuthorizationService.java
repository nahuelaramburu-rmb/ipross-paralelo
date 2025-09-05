package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;

public interface BaseMedicalAuthorizationService<T extends MedicalAuthorization, D extends MedicalAuthorizationDTO> extends BaseService<T, D, Long> {

    T initialize(D medicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException;

    void preProcess(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    T persist(T medicalAuthorization);

    void approve(T medicalAuthorization);

    MedicalAuthorizationProjection postProcess(T medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

}
