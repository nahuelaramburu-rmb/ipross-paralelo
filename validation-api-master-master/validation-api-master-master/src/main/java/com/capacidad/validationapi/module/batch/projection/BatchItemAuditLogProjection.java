package com.capacidad.validationapi.module.batch.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;

import java.util.Set;

public interface BatchItemAuditLogProjection extends AuditLogProjection<Long> {

    Integer getAmount();

    Period getPeriod();

    Set<IdAndNameOnlyProjection> getMedicalCenters();

    Set<PractitionerProjection.Minor> getPractitioners();

}
