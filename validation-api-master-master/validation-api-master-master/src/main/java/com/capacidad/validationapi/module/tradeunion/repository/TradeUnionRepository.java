package com.capacidad.validationapi.module.tradeunion.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TradeUnionRepository extends ExtendedJpaRepository<TradeUnion, Long> {
    Set<IdAndNameOnlyProjection> findAllByBeneficiariesId(long beneficiaryId);

    Set<IdAndNameOnlyProjection> findAllProjectedByNameContainingIgnoreCase(String name);
}
