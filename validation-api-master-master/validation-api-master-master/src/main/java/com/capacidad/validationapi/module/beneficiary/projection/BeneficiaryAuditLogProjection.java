package com.capacidad.validationapi.module.beneficiary.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface BeneficiaryAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    String getStatusUpdateDescription();

}
