package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.math.BigDecimal;

public interface ContractItemProjection extends BaseProjection<Long> {

    BigDecimal getValue();

    IdAndNameOnlyProjection getPractitionerCategory();

    Boolean getRefundable();

}
