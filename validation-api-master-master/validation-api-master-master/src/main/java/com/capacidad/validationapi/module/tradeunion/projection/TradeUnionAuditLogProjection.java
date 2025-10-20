package com.capacidad.validationapi.module.tradeunion.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;

public interface TradeUnionAuditLogProjection extends AuditLogProjection<Long> {

    String getName();

    Boolean getIncludesFamilyGroup();

    Boolean getAutoSettlement();

    Integer getDayOfSettlement();

}
