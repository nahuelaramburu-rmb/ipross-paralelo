package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDiagnosisDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.rating.RatingDTO;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface MedicalAuthorizationService extends BaseService<MedicalAuthorization, MedicalAuthorizationDTO, Long> {

    boolean existsByAuthMedicalCenter(long authorizationId);

    boolean existsByAuthPractitioner(long authorizationId);

    boolean existsByAuthOrganization(long authorizationId) throws ObjectNotFoundException;

    boolean existsByAuthBeneficiaryOrRelative(long authorizationId) throws ObjectNotFoundException;

    int getMedicalCenterAuthorizationTypeAmountInPeriod(MedicalAuthorization medicalAuthorization, AuthorizationType authorizationTypeReference, LocalDateTime from);

    MedicalAuthorizationProjection cancelMedicalAuthorization(long medicalAuthorizationId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    long getMedicalAuthorizationItemParentId(long medicalAuthorizationItemId) throws ObjectNotValidException;

    ByteArrayOutputStream generateReceipt(long medicalAuthorizationId) throws ObjectNotFoundException, ObjectNotValidException;

    List<IdAndNameOnlyProjection> getAllRestrictionTypes();

    MedicalAuthorizationProjection addRating(long medicalAuthorizationId, RatingDTO ratingDTO) throws ObjectNotFoundException, ObjectNotValidException;

    MedicalAuthorizationProjection.Diagnosis updateAuthorizationDiagnosis(long medicalAuthorizationId, MedicalAuthorizationDiagnosisDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    MedicalAuthorizationProjection.Diagnosis getDiagnosis(long medicalAuthorizationId) throws ObjectNotFoundException;

    Set<Message> receiveMessage(long medicalAuthorizationId, MessageDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    Set<Message> dumpAllMessages(long medicalAuthorizationId) throws ObjectNotFoundException;

}
