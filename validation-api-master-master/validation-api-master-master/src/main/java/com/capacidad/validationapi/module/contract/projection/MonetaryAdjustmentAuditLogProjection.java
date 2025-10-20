package com.capacidad.validationapi.module.contract.projection;

import java.math.BigDecimal;

public interface MonetaryAdjustmentAuditLogProjection extends ContractAdjustmentAuditLogProjection {

    BigDecimal getThreshold();

}
