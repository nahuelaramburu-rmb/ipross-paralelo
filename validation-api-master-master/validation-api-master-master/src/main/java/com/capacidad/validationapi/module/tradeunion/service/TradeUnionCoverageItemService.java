package com.capacidad.validationapi.module.tradeunion.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionCoverageItemDTO;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnionCoverageItem;

public interface TradeUnionCoverageItemService extends BaseService<TradeUnionCoverageItem, TradeUnionCoverageItemDTO, Long> {

    TradeUnionCoverageItem create(TradeUnionCoverageItemDTO dto, TradeUnion tradeUnion) throws ObjectNotValidException, ObjectNotFoundException;

}
