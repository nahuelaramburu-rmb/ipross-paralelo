package com.capacidad.validationapi.module.procedure.projection;

import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;

import java.util.Set;

public interface CUDProcedureProjection extends ProcedureProjection {

    Set<ICD10DiseaseProjection> getDiagnosis();

}
