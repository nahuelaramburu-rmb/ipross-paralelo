package com.capacidad.validationapi.module.medicalcoverage.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;

public interface MedicalCoverageAuditLogProjection extends AuditLogProjection<Long> {

    InsurancePlanProjection getInsurancePlan();

    IdAndNameOnlyProjection getRegion();

    IdAndNameOnlyProjection getMedicalPracticeArea();

}
