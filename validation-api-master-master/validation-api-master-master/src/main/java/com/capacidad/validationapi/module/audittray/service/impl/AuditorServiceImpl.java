package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditHistoryRepository;
import com.capacidad.validationapi.module.audittray.repository.AuditorRepository;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class AuditorServiceImpl extends BaseServiceImpl<Auditor, AuditorDTO, Long> implements AuditorService {

    private final AuditorRepository auditorRepository;
    private final AuditHistoryRepository auditorHistoryRepository;

    @Autowired
    public AuditorServiceImpl(AuditorRepository repository,
                              AuditHistoryRepository auditorHistoryRepository) {
        super(repository);
        this.auditorRepository = repository;
        this.auditorHistoryRepository = auditorHistoryRepository;
    }

    @Override
    public Optional<Auditor> findOptionallyBySub(UUID sub) {
        return auditorRepository.findBySub(sub);
    }

    @Override
    public Page<AuditorProjection> getAuditTrayAuditors(long auditTrayId, Pageable pageable) {
        Pageable pageRequest = this.buildPageRequest(pageable);
        return auditorRepository.findAllProjectedAndPaginatedByAuditTraysId(auditTrayId, pageRequest);
    }

    @Override
    public Auditor create(Auditor auditor) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", auditor.getClass(), auditor);
        auditor.setId(null);
        this.validate(auditor);
        auditor.associateChildObjects();
        return auditorRepository.save(auditor);
    }

    @Override
    public AuditHistory findAuditorHistory(UUID auditorSub, UUID auditTrayResourceId, long medicalAuthorizationId) throws ObjectNotFoundException {
        return auditorHistoryRepository.findByAuditorSubAndAuditTrayResourceIdAndMedicalAuthorizationId(auditorSub, auditTrayResourceId, medicalAuthorizationId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "auditor.historyNotFound",
                        String.valueOf(medicalAuthorizationId), auditorSub.toString(), auditTrayResourceId.toString()));
    }

    @Override
    public Auditor findBySub(UUID sub) throws ObjectNotFoundException {
        return auditorRepository
                .findBySub(sub)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "auditor.notFound",
                        sub.toString()));
    }

}
