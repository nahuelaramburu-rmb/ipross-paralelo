package com.capacidad.validationapi.module.batch.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public interface BatchItemProjection extends BaseProjection<Long> {

    @JsonIgnore
    NomenclatorProjection.Minor getNomenclator();

    Integer getAmount();

    Period getPeriod();

    String getMedicalCenter();

    String getPractitioner();

    @JsonIgnore
    Set<IdAndNameOnlyProjection> getMedicalCenters();

    @JsonIgnore
    Set<PractitionerProjection.Minor> getPractitioners();

    interface BatchId extends BaseProjection<Long> {

        BaseProjection<Long> getBatch();

    }

}
