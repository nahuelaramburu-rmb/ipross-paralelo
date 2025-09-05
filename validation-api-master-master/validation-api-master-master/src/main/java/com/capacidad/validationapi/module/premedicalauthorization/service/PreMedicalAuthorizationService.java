package com.capacidad.validationapi.module.premedicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.dto.BasePreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationQrResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.projection.PreMedicalAuthorizationProjection;

public interface PreMedicalAuthorizationService extends BaseService<PreMedicalAuthorization, PreMedicalAuthorizationDTO, Long> {


    <P extends BasePreMedicalAuthorizationDTO> PreMedicalAuthorizationResponseDTO createAndGenerateReceipt(P dto) throws ObjectNotValidException, ObjectNotFoundException;

    <P extends BasePreMedicalAuthorizationDTO> PreMedicalAuthorizationQrResponseDTO createAndGetObjectResponse(P dto) throws ObjectNotValidException, ObjectNotFoundException;

    PreMedicalAuthorizationQrResponseDTO getObjectResponse(long preMedicalAuthorizationId) throws ObjectNotFoundException, ObjectNotValidException;

    PreMedicalAuthorizationProjection cancel(String preMedicalAuthorizationCode, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    PreMedicalAuthorizationProjection cancel(long preMedicalAuthorizationId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    PreMedicalAuthorizationQrResponseDTO validateCodeAndBuildObjectResponse(String preMedicalAuthorizationCode) throws ObjectNotFoundException, ObjectNotValidException;

    PreMedicalAuthorization validateCode(String preMedicalAuthorizationCode) throws ObjectNotFoundException, ObjectNotValidException;

    void processMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    void processMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem, boolean auditing) throws ObjectNotValidException, ObjectNotFoundException;

    void rollbackConsumptionFromMedicalAuthorization(MedicalAuthorization medicalAuthorization);

    boolean existsByAuthBeneficiaryOrRelative(long preMedicalAuthorizationId);

    boolean existsByAuthBeneficiaryOrRelative(String code);

}
