package com.capacidad.validationapi.module.prescription.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface PrescriptionRestrictionProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getMedicalSpecialty();

}
