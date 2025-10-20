package com.capacidad.validationapi.module.settlement.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface SettlementRepository extends ExtendedJpaRepository<Settlement, Long> {

    boolean existsByIdAndPractitionerResourceId(Long settlementId, UUID practitionerResourceId);

    boolean existsByIdAndContractIn(Long settlementId, Collection<Contract> contractIds);

    Optional<Settlement> findByPractitionerIdAndContractIdAndStatusId(Long practitionerId, Long contractId, Long statusId);

    boolean existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween(Long id, Long practitionerId, Long statusId, Long contractId, LocalDateTime from, LocalDateTime to);

    @TenantFilter(active = false)
    Set<Settlement> findAllByStatusIdAndContractAutoSettlementIsTrue(long statusId);

}
