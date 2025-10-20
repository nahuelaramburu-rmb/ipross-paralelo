package com.capacidad.validationapi.module.contract.projection;

import java.math.BigDecimal;

public interface UsageRateAdjustmentAuditLogProjection extends ContractAdjustmentAuditLogProjection {

    BigDecimal getThreshold();

    Long getCapitaAmount();

}
