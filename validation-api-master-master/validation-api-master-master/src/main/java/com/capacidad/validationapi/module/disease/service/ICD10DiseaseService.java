package com.capacidad.validationapi.module.disease.service;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;

import java.util.Set;

public interface ICD10DiseaseService extends BaseService<ICD10Disease, IdDTO<Long>, Long> {

    Set<ICD10DiseaseProjection> findICD10Diseases(String name);

}
