package com.capacidad.validationapi.module.nomenclator.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.MedicalSpecialtyProjection;

import java.util.Set;

public interface MedicalPracticeProjection extends BaseProjection<Long> {

    String getName();

    Set<IdAndNameOnlyProjection> getMedicalSpecialties();
}
