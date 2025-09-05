package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.math.BigDecimal;

public interface ContractItemSpecialPriceAuditLogProjection extends AuditLogProjection<Long> {

    BigDecimal getSpecialValue();

    CalendarEventType getEventType();

    IdAndNameOnlyProjection getChargeType();

}
