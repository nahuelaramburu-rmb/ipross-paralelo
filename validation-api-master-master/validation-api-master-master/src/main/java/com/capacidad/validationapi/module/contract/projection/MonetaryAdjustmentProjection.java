package com.capacidad.validationapi.module.contract.projection;

import java.math.BigDecimal;

public interface MonetaryAdjustmentProjection extends ContractAdjustmentProjection {

    BigDecimal getThreshold();

}
