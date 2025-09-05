package com.capacidad.validationapi.module.audittray.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface AuditHistoryRepository extends ExtendedJpaRepository<AuditHistory, Long> {

    Optional<AuditHistory> findByAuditorSubAndAuditTrayResourceIdAndMedicalAuthorizationId(UUID sub, UUID auditTrayResourceId, Long medicalAuthorizationId);

    Page<AuditHistoryProjection.Minor> findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusIdIsNot(UUID sub, Collection<UUID> auditTrayResourceId, Long statusId, Pageable pageable);

    Page<AuditHistoryProjection.Minor> findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusId
            (UUID sub, Collection<UUID> auditTrayResourceId, Long statusId, Pageable pageable);

    Page<AuditHistoryProjection.Minor> findAllByAuditTrayResourceIdInAndEventAndMedicalAuthorizationStatusIdAndAuditorIsNull
            (Collection<UUID> auditTrayResourceId, AuditTrayEvent event, Long statusId, Pageable pageable);

    Set<AuditHistory> findAllByMedicalAuthorizationId(Long medicalAuthorizationId);

    boolean existsByMedicalAuthorizationIdAndAuditTrayResourceId(Long medicalAuthorizationId, UUID auditTrayResourceId);

    Set<AuditHistory> findAllByEventAndAuditorIsNull(AuditTrayEvent event);

}
