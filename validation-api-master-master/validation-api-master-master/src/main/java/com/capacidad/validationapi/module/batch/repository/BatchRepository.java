package com.capacidad.validationapi.module.batch.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Repository
@TenantFilter
public interface BatchRepository extends ExtendedJpaRepository<Batch, Long> {

    Set<Batch> findAllByStatusIdIn(Set<Long> statusIds);

    @Query("select case when count(b)> 0 then true else false end from Batch b " +
            "where b.status.id in ?1 and " +
            "b.beneficiary.id = ?4 and " +
            "( (b.dateFrom between ?2 and ?3) or (b.dateTo between ?2 and ?3) or (b.dateFrom < ?2  and b.dateTo > ?3) )")
    boolean existsByBeneficiaryAndStatusInPeriod(Set<Long> statusIds, LocalDate from, LocalDate to, long beneficiaryId);

    @Query("select case when count(b)> 0 then true else false end from Batch b " +
            "where b.id <> ?4 and b.status.id in ?1 and " +
            "b.beneficiary.id = ?5 and " +
            "( (b.dateFrom between ?2 and ?3) or (b.dateTo between ?2 and ?3) or (b.dateFrom < ?2  and b.dateTo > ?3) )")
    boolean existsByBeneficiaryAndStatusInPeriodAndIdIsNot(Set<Long> statusIds, LocalDate from, LocalDate to, long id, long beneficiaryId);

    Optional<Batch> findByBeneficiaryIdAndStatusId(Long beneficiaryId, Long statusId);

    Optional<BatchProjection.BeneficiaryId> findBeneficiaryIdProjectionById(long batchId);


}
