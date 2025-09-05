package com.capacidad.validationapi.module.nomenclator.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface NomenclatorProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getMedicalPracticeArea();

    IdAndNameOnlyProjection getMedicalPracticeType();

    Long getNomenclatorCode();

    IdAndNameOnlyProjection getMedicalPractice();

    NomenclatorConfigProjection getNomenclatorConfig();

    interface Minor extends BaseProjection<Long> {
        Long getNomenclatorCode();

        IdAndNameOnlyProjection getMedicalPractice();
    }

}
