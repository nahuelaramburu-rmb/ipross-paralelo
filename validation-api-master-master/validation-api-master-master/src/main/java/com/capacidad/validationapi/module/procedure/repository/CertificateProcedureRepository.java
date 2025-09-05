package com.capacidad.validationapi.module.procedure.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@TenantFilter
public interface CertificateProcedureRepository extends BaseProcedureRepository<CertificateProcedure> {

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from CertificateProcedure p " +
            "where p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedure(Long beneficiaryId, Long statusId);

    @Override
    @Query(value = "select case when count(p)> 0 then true else false end from CertificateProcedure p " +
            "where p.id <> ?3 and p.beneficiary.id = ?1 and p.status.id = ?2")
    boolean findExistentProcedureIdIsNot(Long beneficiaryId, Long statusId, Long procedureId);

}
