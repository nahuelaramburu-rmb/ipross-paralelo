package com.capacidad.validationapi.module.contract.projection;

import java.math.BigDecimal;

public interface UsageRateAdjustmentProjection extends ContractAdjustmentProjection {

    BigDecimal getThreshold();

    Long getCapitaAmount();

}
