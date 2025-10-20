package com.capacidad.validationapi.module.procedure.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface ProcedureAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

}
