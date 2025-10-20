package com.capacidad.validationapi.module.audittray.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@TenantFilter
public interface AuditorRepository extends ExtendedJpaRepository<Auditor, Long> {

    Optional<Auditor> findBySub(UUID sub);

    Page<AuditorProjection> findAllProjectedAndPaginatedByAuditTraysId(long auditTrayId, Pageable pageable);

}
