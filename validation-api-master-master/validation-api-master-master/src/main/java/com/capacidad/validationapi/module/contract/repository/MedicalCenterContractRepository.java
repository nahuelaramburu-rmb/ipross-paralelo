package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface MedicalCenterContractRepository extends BaseContractRepository<MedicalCenterContract, Long> {

    @Query("select case when count(c)> 0 then true else false end from MedicalCenterContract c " +
            "where c.medicalCenter.id = ?1 and " +
            "( (c.dateFrom between ?2 and ?3) or (c.dateTo between ?2 and ?3) or (c.dateFrom < ?2 and c.dateTo > ?3) )")
    boolean existsByMedicalCenterIdAndPeriod(Long medicalCenterId, LocalDate dateFrom, LocalDate dateTo);

    @Query("select case when count(c)> 0 then true else false end from MedicalCenterContract c " +
            "where c.id <> ?1 and " +
            "c.medicalCenter.id = ?2 and " +
            "( (c.dateFrom between ?3 and ?4) or (c.dateTo between ?3 and ?4) or (c.dateFrom < ?3  and c.dateTo > ?4) )")
    boolean existsByIdNotAndMedicalCenterIdAndPeriod(Long medicalCenterContractId, Long medicalCenterId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByIdAndMedicalCenterResourceId(Long contractId, UUID resourceId);

    Set<Contract> findAllByMedicalCenterResourceId(UUID resourceId);

}
