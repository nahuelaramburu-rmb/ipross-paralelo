package com.capacidad.validationapi.module.tradeunion.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionCoverageItemDTO;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnionCoverageItem;
import com.capacidad.validationapi.module.tradeunion.repository.TradeUnionCoverageItemRepository;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionCoverageItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeUnionCoverageItemServiceImpl extends BaseServiceImpl<TradeUnionCoverageItem, TradeUnionCoverageItemDTO, Long> implements TradeUnionCoverageItemService {

    private final TradeUnionCoverageItemRepository tradeUnionCoverageItemRepository;

    @Autowired
    public TradeUnionCoverageItemServiceImpl(TradeUnionCoverageItemRepository tradeUnionCoverageItemRepository) {
        super(tradeUnionCoverageItemRepository);
        this.tradeUnionCoverageItemRepository = tradeUnionCoverageItemRepository;
    }

    @Override
    public TradeUnionCoverageItem create(TradeUnionCoverageItemDTO dto, TradeUnion tradeUnion) throws ObjectNotValidException, ObjectNotFoundException {
        TradeUnionCoverageItem object = this.mapDtoToInput(dto);
        object.setTradeUnion(tradeUnion);
        this.validate(object);
        return tradeUnionCoverageItemRepository.save(object);
    }

}
