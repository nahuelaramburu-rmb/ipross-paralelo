package com.capacidad.validationapi.module.tradeunion.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionDTO;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;

import java.util.Set;

public interface TradeUnionService extends BaseService<TradeUnion, TradeUnionDTO, Long> {

    Set<IdAndNameOnlyProjection> findAllByBeneficiaryId(long beneficiaryId);

    Set<IdAndNameOnlyProjection> getTradeUnions(String name);

}
