package com.capacidad.validationapi.module.prescription.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface PrescriptionAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    String getCancellationReason();

}
