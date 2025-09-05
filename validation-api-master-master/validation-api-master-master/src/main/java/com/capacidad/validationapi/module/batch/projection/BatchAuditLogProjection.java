package com.capacidad.validationapi.module.batch.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.time.LocalDate;
import java.util.Set;

public interface BatchAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    LocalDate getDateFrom();

    LocalDate getDateTo();

    String getStatusUpdateDescription();

    String getDescription();

    Set<ICD10DiseaseProjection> getDiagnosis();

}
