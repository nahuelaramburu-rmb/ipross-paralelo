package com.capacidad.validationapi.module.disease.service.impl;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.disease.repository.ICD10DiseaseRepository;
import com.capacidad.validationapi.module.disease.service.ICD10DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class ICD10DiseaseServiceImpl extends BaseServiceImpl<ICD10Disease, IdDTO<Long>, Long> implements ICD10DiseaseService {

    private final ICD10DiseaseRepository icd10DiseaseRepository;

    @Autowired
    public ICD10DiseaseServiceImpl(ICD10DiseaseRepository icd10DiseaseRepository) {
        super(icd10DiseaseRepository);
        this.icd10DiseaseRepository = icd10DiseaseRepository;
    }

    @Override
    public Set<ICD10DiseaseProjection> findICD10Diseases(String name) {
        if (name.length() >= 3)
            return icd10DiseaseRepository
                    .findTop50ByNameContainingIgnoreCaseOrCodeStartingWithIgnoreCase
                            (name, name);
        return Collections.emptySet();
    }
}
