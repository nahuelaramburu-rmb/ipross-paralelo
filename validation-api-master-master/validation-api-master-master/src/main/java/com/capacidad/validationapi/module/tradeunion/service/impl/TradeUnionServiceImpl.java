package com.capacidad.validationapi.module.tradeunion.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.tradeunion.dto.TradeUnionDTO;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import com.capacidad.validationapi.module.tradeunion.repository.TradeUnionRepository;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class TradeUnionServiceImpl extends BaseServiceImpl<TradeUnion, TradeUnionDTO, Long> implements TradeUnionService {

    private final TradeUnionRepository tradeUnionRepository;

    @Autowired
    public TradeUnionServiceImpl(TradeUnionRepository tradeUnionRepository) {
        super(tradeUnionRepository);
        this.tradeUnionRepository = tradeUnionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long tradeUnionId) throws ObjectNotValidException, ObjectNotFoundException {
        var tradeUnion = this.findById(tradeUnionId);
        var deletionToken = UUID.randomUUID();
        tradeUnion.getAddress().setDeleted(true);
        tradeUnion.getAddress().setDeletionToken(deletionToken);
        tradeUnion.setDeleted(true);
        tradeUnion.setDeletionToken(deletionToken);
        tradeUnion.getBeneficiaries().forEach(b -> b.getTradeUnions().remove(tradeUnion));
        tradeUnionRepository.save(tradeUnion);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(tradeUnion, tradeUnionRepository));
        return this.buildIdResponse(tradeUnion.getId());
    }

    @Override
    public Set<IdAndNameOnlyProjection> findAllByBeneficiaryId(long beneficiaryId) {
        return tradeUnionRepository.findAllByBeneficiariesId(beneficiaryId);
    }

    @Override
    public Set<IdAndNameOnlyProjection> getTradeUnions(String name) {
        return tradeUnionRepository.findAllProjectedByNameContainingIgnoreCase(name);
    }
}
