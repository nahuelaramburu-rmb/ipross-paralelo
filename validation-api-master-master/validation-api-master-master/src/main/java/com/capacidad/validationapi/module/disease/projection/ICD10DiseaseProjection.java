package com.capacidad.validationapi.module.disease.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

public interface ICD10DiseaseProjection extends BaseProjection<Long> {

    String getCode();

    String getName();

}
