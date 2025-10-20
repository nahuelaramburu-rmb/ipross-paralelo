package com.capacidad.validationapi.module.medicalcoverage.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageItemDTO;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface MedicalCoverageItemService extends BaseService<MedicalCoverageItem, MedicalCoverageItemDTO, Long> {

    MedicalCoverageItem create(MedicalCoverageItemDTO dto, MedicalCoverage medicalCoverage) throws ObjectNotValidException;

    Page<MedicalCoverageItemProjection> getMedicalCoverageItems(long medicalCoverageId, Pageable pageable);

    Optional<MedicalCoverageItem> findMedicalCoverageItem(long medicalCoverageId, long nomenclatorId);

    Set<MedicalCoverageItemProjection> findAllMedicalCoverageItems(long medicalCoverageId, Set<Long> nomenclatorIds);

    JsonNode delete(MedicalCoverageItem medicalCoverageItem);

}
