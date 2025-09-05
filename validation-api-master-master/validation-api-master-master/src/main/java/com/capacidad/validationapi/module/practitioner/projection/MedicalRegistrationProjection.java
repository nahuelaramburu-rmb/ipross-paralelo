package com.capacidad.validationapi.module.practitioner.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface MedicalRegistrationProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getOrganization();

    String getRegistrationCode();

}
