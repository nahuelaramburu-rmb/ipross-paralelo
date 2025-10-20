package com.capacidad.validationapi.module.audittray.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryAssignDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface AuditHistoryService extends BaseService<AuditHistory, IdDTO<Long>, Long> {

    AuditHistory persistAndFlush(AuditHistory auditHistory);

    void associateAuditHistoryWithAuditor(long auditHistoryId) throws ObjectNotFoundException, ObjectNotValidException;

    AuditHistory assignAuditHistory(long auditHistoryId, AuditHistoryAssignDTO auditHistoryAssignDTO) throws ObjectNotFoundException, ObjectNotValidException;

    Page<AuditHistoryProjection.Minor> getHistory(Collection<UUID> auditTrayResourceId, boolean pending, Pageable pageable);

    Page<AuditHistoryProjection.Minor> getOfflineHistory(Collection<UUID> auditTrayResourceId, Pageable pageable) throws ObjectNotFoundException;

    MedicalAuthorizationProjection.Status resolveIssue(long auditHistoryId, AuditHistoryResolutionDTO auditHistoryResolutionDTO) throws ObjectNotValidException, ObjectNotFoundException;

    Set<AuditHistory> findByMedicalAuthorizationId(long medicalAuthorizationId);

    AuditHistoryProjection getAuditHistory(long auditHistoryId) throws ObjectNotFoundException, ObjectNotValidException;

    void resolveUnassignedAuditHistories();

}
