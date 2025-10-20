package com.capacidad.validationapi.module.procedure.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@TenantFilter
public interface ProcedureRepository extends BaseProcedureRepository<Procedure> {

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from Procedure p " +
            "where p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedure(Long beneficiaryId, Long statusId);

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from Procedure p " +
            "where p.id <> ?3 and p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedureIdIsNot(Long beneficiaryId, Long statusId, Long procedureId);


    @Query(value = "select p from Procedure p " +
            "where p.beneficiary.id = ?1 and " +
            "p.status.id = ?2 " +
            "and (p.expiration is null or p.expiration >= current_date)")
    Set<Procedure> findAllApprovedNotExpiredByBeneficiaryId(Long beneficiaryId, Long statusId);

    @Query(value = "select p from Procedure p " +
            "where p.id in ?1 and " +
            "p.status.id = ?2 " +
            "and (p.expiration is null or p.expiration >= current_date)")
    Set<Procedure> findAllApprovedNotExpiredByIdIn(Set<Long> procedureIds, Long statusId);

    Set<Procedure> findAllByStatusIdAndExpirationNotNull(Long statusId);

    Optional<ProcedureProjection.BeneficiaryId> findBeneficiaryIdProjectionById(long procedureId);

}
