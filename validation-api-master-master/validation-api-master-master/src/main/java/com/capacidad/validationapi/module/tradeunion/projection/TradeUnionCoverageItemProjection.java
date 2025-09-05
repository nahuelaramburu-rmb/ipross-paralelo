package com.capacidad.validationapi.module.tradeunion.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;

import java.math.BigDecimal;

public interface TradeUnionCoverageItemProjection extends BaseProjection<Long> {

    NomenclatorProjection getNomenclator();

    Integer getQuantity();

    Period getPeriod();

    BigDecimal getPercentage();

}
