package com.capacidad.validationapi.module.tradeunion.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.model.Period;

import java.math.BigDecimal;

public interface TradeUnionCoverageItemAuditLogProjection extends AuditLogProjection<Long> {

    Integer getQuantity();

    Period getPeriod();

    BigDecimal getPercentage();

}
