package com.capacidad.validationapi.module.audittray.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.base.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AuditorService extends BaseService<Auditor, AuditorDTO, Long> {

    Optional<Auditor> findOptionallyBySub(UUID sub);

    Auditor findBySub(UUID sub) throws ObjectNotFoundException;

    Page<AuditorProjection> getAuditTrayAuditors(long auditTrayId, Pageable pageable);

    Auditor create(Auditor auditor) throws ObjectNotValidException, ObjectNotFoundException;

    AuditHistory findAuditorHistory(UUID auditorSub, UUID auditTrayResourceId, long medicalAuthorizationId) throws ObjectNotFoundException;

}
