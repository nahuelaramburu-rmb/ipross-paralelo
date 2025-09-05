package com.capacidad.validationapi.module.practitioner.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface PractitionerAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    String getStatusUpdateDescription();

}
