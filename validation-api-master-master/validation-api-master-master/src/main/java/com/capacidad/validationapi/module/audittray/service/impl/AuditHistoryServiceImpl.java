package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryAssignDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.model.*;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditHistoryRepository;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.audittray.service.AuditHistorySupportService;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuditHistoryServiceImpl extends BaseServiceImpl<AuditHistory, IdDTO<Long>, Long> implements AuditHistoryService {

    private final AuditHistoryRepository auditHistoryRepository;
    private final AuditorService auditorService;
    private final AuditTrayService auditTrayService;
    private final AuditHistorySupportService auditHistorySupportService;

    @Autowired
    public AuditHistoryServiceImpl(AuditHistoryRepository repository,
                                   AuditorService auditorService,
                                   AuditTrayService auditTrayService,
                                   AuditHistorySupportService auditHistorySupportService) {
        super(repository);
        this.auditHistoryRepository = repository;
        this.auditorService = auditorService;
        this.auditTrayService = auditTrayService;
        this.auditHistorySupportService = auditHistorySupportService;
    }

    @Override
    public AuditHistory persistAndFlush(AuditHistory auditHistory) {
        auditHistory.associateChildObjects();
        return auditHistoryRepository.saveAndFlush(auditHistory);
    }

    @Override
    public void associateAuditHistoryWithAuditor(long auditHistoryId) throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = this.findById(auditHistoryId);
        Auditor auditor = auditorService.findBySub(SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
        validateAuditorAuditTray(auditHistory.getAuditTray(), auditor);
        auditHistory.setAuditor(auditor);
        auditHistoryRepository.save(auditHistory);
    }

    private void validateAuditorAuditTray(AuditTray auditTray, Auditor auditor) throws ObjectNotValidException {
        if (!auditor.getAuditTrays().contains(auditTray))
            throw new ObjectNotValidException("auditHistory.auditorInvalidAuditTray", auditTray.getName());
    }

    @Override
    public AuditHistory assignAuditHistory(long auditHistoryId, AuditHistoryAssignDTO auditHistoryAssignDTO) throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = this.findById(auditHistoryId);
        if (auditHistoryRepository.existsByMedicalAuthorizationIdAndAuditTrayResourceId
                (auditHistory.getMedicalAuthorization().getId(), auditHistoryAssignDTO.getAuditTrayResourceId()))
            throw new ObjectAlreadyExistsException(
                    "auditHistory.auditTrayAlreadyContainsMedAuth",
                    auditHistory.getAuditTray().getName(), auditHistory.getMedicalAuthorization().getId().toString());
        Auditor authAuditor = auditorService.findBySub(SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
        Auditor historyAuditor = auditHistory.getAuditor();
        if (historyAuditor == null || !historyAuditor.getId().equals(authAuditor.getId()))
            throw new ObjectNotValidException(
                    "auditHistory.doesNotBelongToAuditor",
                    String.valueOf(auditHistoryId), authAuditor.getUsername());
        AuditTray newAuditTray = auditTrayService.findByResourceId(auditHistoryAssignDTO.getAuditTrayResourceId());
        auditHistory.setReason(auditHistoryAssignDTO.getReason());
        auditHistory.setFromAuditTray(auditHistory.getAuditTray());
        auditHistory.setFromAuditor(authAuditor);
        auditHistory.setAuditor(null);
        auditHistory.setAuditTray(newAuditTray);
        return auditHistory;
    }

    @Override
    public Page<AuditHistoryProjection.Minor> getHistory(Collection<UUID> auditTrayResourceId, boolean pending, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, ModelConstants.MODIFIED_AT);
        UUID auditorSub = SecurityUtils.getAuthenticatedAuthoritySub().orElse(null);
        return pending ? auditHistoryRepository.findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusId
                (auditorSub, auditTrayResourceId, StatusReference.VALIDATION_PENDING.getId(), pageRequest)
                : auditHistoryRepository.findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusIdIsNot
                (auditorSub, auditTrayResourceId, StatusReference.VALIDATION_PENDING.getId(), pageRequest);

    }

    @Override
    public Page<AuditHistoryProjection.Minor> getOfflineHistory(Collection<UUID> auditTrayResourceId, Pageable pageable) throws ObjectNotFoundException {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, ModelConstants.MODIFIED_AT);
        Auditor auditor = auditorService.findBySub(SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
        Set<AuditTray> auditTrays = auditTrayService.findAllByResourceId(auditTrayResourceId);
        auditTrays.forEach(throwingConsumer(auditTray -> validateAuditorAuditTray(auditTray, auditor)));
        return auditHistoryRepository.findAllByAuditTrayResourceIdInAndEventAndMedicalAuthorizationStatusIdAndAuditorIsNull
                (auditTrayResourceId, AuditTrayEvent.OFFLINE_ISSUE, StatusReference.VALIDATION_PENDING.getId(), pageRequest);
    }

    @Override
    public MedicalAuthorizationProjection.Status resolveIssue(long auditHistoryId, AuditHistoryResolutionDTO auditHistoryResolutionDTO) throws ObjectNotValidException, ObjectNotFoundException {
        AuditHistory auditHistory = this.findById(auditHistoryId);
        AuditHistoryItem auditHistoryItem = auditHistory.getHistoryItems().stream()
                .filter(item -> item.getMedicalAuthorizationItem().getId().equals(auditHistoryResolutionDTO.getMedicalAuthorizationItemId()))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException(
                        "auditHistory.medAuthDoesNotExistInAuditHistory",
                        String.valueOf(auditHistoryId)));
        Auditor authAuditor = auditorService.findBySub(SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
        Auditor historyAuditor = auditHistory.getAuditor();
        if (historyAuditor == null || !historyAuditor.getId().equals(authAuditor.getId()))
            throw new ObjectNotValidException(
                    "auditHistory.doesNotBelongToAuditor",
                    String.valueOf(auditHistoryId), authAuditor.getUsername());
        return auditHistorySupportService.processAuditResolution(auditHistoryItem.getMedicalAuthorizationItem(), auditHistoryResolutionDTO);
    }

    @Override
    public Set<AuditHistory> findByMedicalAuthorizationId(long medicalAuthorizationId) {
        return auditHistoryRepository.findAllByMedicalAuthorizationId(medicalAuthorizationId);
    }

    @Override
    public AuditHistoryProjection getAuditHistory(long auditHistoryId) throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = this.findById(auditHistoryId);
        Auditor authAuditor = auditorService.findBySub(SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
        if (!auditHistory.containsAuditor(authAuditor))
            throw new ObjectNotValidException("auditHistory.auditorInvalidAuditTray", auditHistory.getAuditTray().getName());
        return this.getProjectionFactory().createProjection(AuditHistoryProjection.class, auditHistory);
    }

    @Override
    public void resolveUnassignedAuditHistories() {
        Set<AuditHistory> unassignedAuditHistories = auditHistoryRepository.findAllByEventAndAuditorIsNull(AuditTrayEvent.NEW_ISSUE);
        unassignedAuditHistories.forEach(history -> {
            if (Duration.between(history.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 2)
                history.setEvent(AuditTrayEvent.OFFLINE_ISSUE);
        });
        auditHistoryRepository.saveAll(unassignedAuditHistories);
    }

}