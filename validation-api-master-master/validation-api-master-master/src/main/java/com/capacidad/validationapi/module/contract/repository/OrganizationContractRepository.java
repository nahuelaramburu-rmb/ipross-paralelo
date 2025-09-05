package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.organization.model.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface OrganizationContractRepository extends BaseContractRepository<OrganizationContract, Long> {

    @Query("select case when count(c)> 0 then true else false end from OrganizationContract c " +
            "where c.organization.id = ?1 and " +
            "( (c.dateFrom between ?2 and ?3) or (c.dateTo between ?2 and ?3) or (c.dateFrom < ?2 and c.dateTo > ?3) )")
    boolean existsByOrganizationIdAndPeriod(Long organizationId, LocalDate dateFrom, LocalDate dateTo);

    @Query("select case when count(c)> 0 then true else false end from OrganizationContract c " +
            "where c.id <> ?1 and " +
            "c.organization.id = ?2 and " +
            "( (c.dateFrom between ?3 and ?4) or (c.dateTo between ?3 and ?4) or (c.dateFrom < ?3  and c.dateTo > ?4) )")
    boolean existsByIdNotAndOrganizationIdAndPeriod(Long organizationContractId, Long organizationId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByIdAndOrganizationResourceId(Long contractId, UUID resourceId);

    Set<Contract> findAllByOrganizationOrOrganization(Organization organization, Organization relatedOrganization);

}
