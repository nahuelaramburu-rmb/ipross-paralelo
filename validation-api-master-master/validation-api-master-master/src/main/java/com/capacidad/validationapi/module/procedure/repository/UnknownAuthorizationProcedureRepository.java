package com.capacidad.validationapi.module.procedure.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.procedure.model.UnknownAuthorizationProcedure;
import com.capacidad.validationapi.module.procedure.projection.UnknownAuthorizationProcedureProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@TenantFilter
public interface UnknownAuthorizationProcedureRepository extends BaseProcedureRepository<UnknownAuthorizationProcedure> {

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from UnknownAuthorizationProcedure p " +
            "where p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedure(Long beneficiaryId, Long statusId);

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from UnknownAuthorizationProcedure p " +
            "where p.id <> ?3 and p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedureIdIsNot(Long beneficiaryId, Long statusId, Long procedureId);

    Optional<UnknownAuthorizationProcedureProjection> findByMedicalAuthorizationId(long authorizationId);

}
