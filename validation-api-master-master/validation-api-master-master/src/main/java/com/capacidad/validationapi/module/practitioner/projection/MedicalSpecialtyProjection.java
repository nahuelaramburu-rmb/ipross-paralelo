package com.capacidad.validationapi.module.practitioner.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface MedicalSpecialtyProjection extends BaseProjection<Long> {
    String getName();

    interface Full extends MedicalSpecialtyProjection {
        IdAndNameOnlyProjection getMedicalSpecialtyType();
    }

}
