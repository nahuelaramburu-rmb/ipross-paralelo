package com.capacidad.validationapi.module.medicalcoverage.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;

public interface MedicalCoverageProjection extends IdAndNameOnlyProjection {

    InsurancePlanProjection getInsurancePlan();

    IdAndNameOnlyProjection getRegion();

    IdAndNameOnlyProjection getMedicalPracticeArea();

    IdAndNameOnlyProjection getCity();

    interface Reduced extends IdAndNameOnlyProjection {

        InsurancePlanProjection getInsurancePlan();

    }

}
