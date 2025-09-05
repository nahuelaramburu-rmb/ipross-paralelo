package com.capacidad.validationapi.module.settlement.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@TenantFilter
public interface SettlementItemRepository extends ExtendedJpaRepository<SettlementItem, Long> {

    Set<SettlementItem> findAllByMedicalAuthorizationId(Long medicalAuthorizationId);

    @Query("select s from SettlementItem s " +
            "inner join s.medicalAuthorization as med " +
            "inner join med.beneficiary as b " +
            "where s.settlement.id = ?1 " +
            "and b.beneficiaryCode = ?2")
    Set<SettlementItem> findAllBySettlementIdAndBeneficiaryCode(long settlementId, String beneficiaryCode);

    @Query("select s from SettlementItem s " +
            "where s.settlement.id = ?1 " +
            "and s.id in ?2")
    Set<SettlementItem> findAllBySettlementIdAndItemIdsIn(long settlementId, Set<Long> itemIds);

}
