package com.capacidad.validationapi.module.tradeunion.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;

import java.util.UUID;

public interface TradeUnionProjection extends BaseProjection<Long> {

    String getName();

    AddressProjection getAddress();

    UUID getResourceId();

    Boolean getIncludesFamilyGroup();

    Boolean getAutoSettlement();

    Integer getDayOfSettlement();

}
