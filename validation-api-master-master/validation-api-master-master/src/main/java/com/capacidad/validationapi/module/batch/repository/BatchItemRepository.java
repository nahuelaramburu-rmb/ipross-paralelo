package com.capacidad.validationapi.module.batch.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@TenantFilter
public interface BatchItemRepository extends ExtendedJpaRepository<BatchItem, Long> {

    Optional<BatchItemProjection.BatchId> findBatchIdProjectionById(long batchId);

    Optional<BatchItem> findByBatchIdAndNomenclatorId(long batchId, long nomenclatorId);

}
