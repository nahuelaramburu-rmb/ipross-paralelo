package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageItemDTO;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import com.capacidad.validationapi.module.medicalcoverage.repository.MedicalCoverageItemRepository;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
public class MedicalCoverageItemServiceImpl extends BaseServiceImpl<MedicalCoverageItem, MedicalCoverageItemDTO, Long> implements MedicalCoverageItemService {

    private final MedicalCoverageItemRepository medicalCoverageItemRepository;

    @Autowired
    public MedicalCoverageItemServiceImpl(MedicalCoverageItemRepository repository) {
        super(repository);
        this.medicalCoverageItemRepository = repository;
    }

    @Override
    public void validate(MedicalCoverageItem object) throws ObjectNotValidException {
        if (object.getChargeValue() != null ^ object.getChargeType() != null)
            throw new ObjectNotValidException("medicalCoverageItem.invalidChargeTypeOrValue");
        if (object.getFixedMaxDays() != null ^ object.getFixedMaxQuantity() != null)
            throw new ObjectNotValidException("medicalCoverageItem.invalidFixedMaxDaysOrQuantity");
        if (object.getFreeMaxDays() != null ^ object.getFreeMaxQuantity() != null)
            throw new ObjectNotValidException("medicalCoverageItem.invalidFreeMaxDaysOrQuantity");
        if (object.getAgeFrom() != null ^ object.getAgeTo() != null)
            throw new ObjectNotValidException("medicalCoverageItem.invalidAgeFromOrAgeTo");
        if (object.getAgeFrom() != null && object.getAgeFrom() > object.getAgeTo())
            throw new ObjectNotValidException("medicalCoverageItem.invalidAgeTo");
    }

    @Override
    public MedicalCoverageItem create(MedicalCoverageItemDTO dto, MedicalCoverage medicalCoverage) throws ObjectNotValidException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        MedicalCoverageItem object = this.mapDtoToInput(dto);
        object.setMedicalCoverage(medicalCoverage);
        this.validate(object);
        MedicalCoverageItem objectResponse = medicalCoverageItemRepository.save(object);
        log.info("create - void: {}({})", object.getClass(), object);
        return objectResponse;
    }

    @Override
    public Page<MedicalCoverageItemProjection> getMedicalCoverageItems(long medicalCoverageId, Pageable pageRequest) {
        Pageable pageable = this.buildPageRequest(pageRequest);
        return medicalCoverageItemRepository.findAllProjectedByMedicalCoverageId(medicalCoverageId, pageable);
    }

    @Override
    public Optional<MedicalCoverageItem> findMedicalCoverageItem(long medicalCoverageId, long nomenclatorId) {
        return medicalCoverageItemRepository.findByMedicalCoverageIdAndNomenclatorId(medicalCoverageId, nomenclatorId);
    }

    @Override
    public Set<MedicalCoverageItemProjection> findAllMedicalCoverageItems(long medicalCoverageId, Set<Long> nomenclatorIds) {
        return medicalCoverageItemRepository.findAllByMedicalCoverageIdAndNomenclatorIdIn(medicalCoverageId, nomenclatorIds);
    }

    @Override
    public JsonNode delete(MedicalCoverageItem medicalCoverageItem) {
        medicalCoverageItem.setDeleted(true);
        medicalCoverageItem.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(medicalCoverageItem);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(medicalCoverageItem, this.getRepository()));
        return buildIdResponse(medicalCoverageItem.getId());
    }

}
