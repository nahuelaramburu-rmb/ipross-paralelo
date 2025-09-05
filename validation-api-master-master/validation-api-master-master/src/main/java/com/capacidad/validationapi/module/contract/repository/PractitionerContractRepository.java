package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
@TenantFilter
public interface PractitionerContractRepository extends BaseContractRepository<PractitionerContract, Long> {

    @Query("select case when count(c)> 0 then true else false end from PractitionerContract c " +
            "where c.practitioner.id = ?1 and " +
            "( (c.dateFrom between ?2 and ?3) or (c.dateTo between ?2 and ?3) or (c.dateFrom < ?2 and c.dateTo > ?3) )")
    boolean existsByPractitionerIdAndPeriod(Long practitionerId, LocalDate dateFrom, LocalDate dateTo);

    @Query("select case when count(c)> 0 then true else false end from PractitionerContract c " +
            "where c.id <> ?1 and " +
            "c.practitioner.id = ?2 and " +
            "( (c.dateFrom between ?3 and ?4) or (c.dateTo between ?3 and ?4) or (c.dateFrom < ?3  and c.dateTo > ?4) )")
    boolean existsByIdNotAndPractitionerIdAndPeriod(Long practitionerContractId, Long practitionerId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByIdAndPractitionerResourceId(Long contractId, UUID resourceId);

}
