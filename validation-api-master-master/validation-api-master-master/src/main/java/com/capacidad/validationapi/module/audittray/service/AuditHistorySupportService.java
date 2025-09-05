package com.capacidad.validationapi.module.audittray.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;

public interface AuditHistorySupportService {

    MedicalAuthorizationProjection.Status processAuditResolution(MedicalAuthorizationItem medicalAuthorizationItem, AuditHistoryResolutionDTO auditHistoryResolutionDTO) throws ObjectNotValidException, ObjectNotFoundException;

}
