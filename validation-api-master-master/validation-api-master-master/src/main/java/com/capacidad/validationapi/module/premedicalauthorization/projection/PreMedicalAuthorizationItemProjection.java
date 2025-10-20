package com.capacidad.validationapi.module.premedicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;

import java.math.BigDecimal;

public interface PreMedicalAuthorizationItemProjection extends BaseProjection<Long> {
    NomenclatorProjection.Minor getNomenclator();

    Integer getQuantity();

    Integer getRemaining();

    BigDecimal getChargeUnitPrice();

}
