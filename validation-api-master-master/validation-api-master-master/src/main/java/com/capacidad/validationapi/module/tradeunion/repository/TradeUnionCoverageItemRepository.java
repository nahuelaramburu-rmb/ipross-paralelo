package com.capacidad.validationapi.module.tradeunion.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnionCoverageItem;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeUnionCoverageItemRepository extends ExtendedJpaRepository<TradeUnionCoverageItem, Long> {
}
