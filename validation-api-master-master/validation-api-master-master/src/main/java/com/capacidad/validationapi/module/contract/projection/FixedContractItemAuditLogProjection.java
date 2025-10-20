package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;

import java.math.BigDecimal;
import java.util.List;

public interface FixedContractItemAuditLogProjection extends AuditLogProjection<Long> {

    BigDecimal getValue();

    List<ContractItemSpecialPriceAuditLogProjection> getSpecialPrices();

}
