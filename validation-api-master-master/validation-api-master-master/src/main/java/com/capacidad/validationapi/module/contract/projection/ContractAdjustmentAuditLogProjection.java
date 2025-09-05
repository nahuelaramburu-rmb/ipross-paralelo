package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.time.LocalDateTime;

public interface ContractAdjustmentAuditLogProjection extends AuditLogProjection<Long> {

    Period getPeriod();

    LocalDateTime getCreatedAt();

    IdAndNameOnlyProjection getRestrictionType();

}
