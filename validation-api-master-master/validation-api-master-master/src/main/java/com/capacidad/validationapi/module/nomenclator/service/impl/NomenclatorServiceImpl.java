package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorDTO;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearch;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearchType;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.nomenclator.repository.NomenclatorRepository;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorGroupService;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorSupportService;
import com.capacidad.validationapi.module.practitioner.repository.MedicalSpecialtyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class NomenclatorServiceImpl extends BaseServiceImpl<Nomenclator, NomenclatorDTO, Long> implements NomenclatorService {

    private final NomenclatorRepository nomenclatorRepository;
    private final MedicalSpecialtyRepository medicalSpecialtyRepository;
    private final NomenclatorGroupService nomenclatorGroupService;
    private final NomenclatorSupportService nomenclatorSupportService;

    @Autowired
    public NomenclatorServiceImpl(NomenclatorRepository repository,
                                  MedicalSpecialtyRepository medicalSpecialtyRepository,
                                  NomenclatorGroupService nomenclatorGroupService,
                                  NomenclatorSupportService nomenclatorSupportService) {
        super(repository);
        this.nomenclatorRepository = repository;
        this.medicalSpecialtyRepository = medicalSpecialtyRepository;
        this.nomenclatorGroupService = nomenclatorGroupService;
        this.nomenclatorSupportService = nomenclatorSupportService;
    }

    @Override
    public List<NomenclatorSearch> searchNomenclatorsAndGroups(String name) {
        List<NomenclatorProjection.Minor> nomenclators = nomenclatorRepository
                .findAllByNomenclatorCodeContainingIgnoreCaseOrMedicalPracticeNameContainingIgnoreCase
                        (name, name);
        List<NomenclatorSearch> nomenclatorSearches = nomenclators.stream()
                .map(n -> {
                    NomenclatorSearch search = new NomenclatorSearch();
                    search.setType(NomenclatorSearchType.NOMENCLATOR);
                    search.setContent(n);
                    return search;
                })
                .collect(Collectors.toList());
        List<NomenclatorSearch> nomenclatorGroupSearches = nomenclatorGroupService.getNomenclatorSearches(name);
        nomenclatorSearches.addAll(nomenclatorGroupSearches);
        return nomenclatorSearches;
    }

    @Override
    public Nomenclator findByNomenclatorCode(String nomenclatorCode) throws ObjectNotFoundException {
        return nomenclatorRepository.findByNomenclatorCode(nomenclatorCode)
                .orElseThrow(() -> new ObjectNotFoundException("nomenclator.codeNotFound", nomenclatorCode));
    }

    @Override
    public List<NomenclatorProjection.Minor> getMedicalSpecialtyNomenclators(long medicalSpecialtyId) throws ObjectNotFoundException {
        if (!medicalSpecialtyRepository.existsById(medicalSpecialtyId))
            throw new ObjectNotFoundException("nomenclator.specialtyNotFound", String.valueOf(medicalSpecialtyId));
        return nomenclatorRepository.findAllByMedicalPracticeMedicalSpecialtiesId(medicalSpecialtyId);
    }

    @Override
    public Page<NomenclatorProjection.Minor> getAuditTrayNomenclators(long auditTrayId, Pageable pageable) {
        Pageable pageRequest = this.buildPageRequest(pageable);
        return nomenclatorRepository.findAllProjectedAndPaginatedByAuditTraysId(auditTrayId, pageRequest);
    }

    @Override
    public JsonNode delete(Long id) throws ObjectNotFoundException {
        Nomenclator nomenclator = this.findById(id);
        return nomenclatorSupportService.delete(nomenclator);
    }

}
